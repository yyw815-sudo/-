package com.medical.service.ai;

import com.medical.entity.DrugInteraction;
import com.medical.entity.Medicine;
import com.medical.repository.DrugInteractionRepository;
import com.medical.repository.MedicineInteractionRepository;
import com.medical.repository.MedicineRepository;
import com.medical.service.ai.context.PatientContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PlanValidator {

    private final MedicineRepository medicineRepository;
    private final DrugInteractionRepository drugInteractionRepository;
    private final MedicineInteractionRepository medicineInteractionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Pattern DOSE_PATTERN = Pattern.compile("^(\\d+\\.?\\d*)\\s*(mg|g|ml|μl|粒|片|袋|支|瓶|丸|胶囊)?$", Pattern.CASE_INSENSITIVE);

    public PlanValidator(MedicineRepository medicineRepository, DrugInteractionRepository drugInteractionRepository, MedicineInteractionRepository medicineInteractionRepository) {
        this.medicineRepository = medicineRepository;
        this.drugInteractionRepository = drugInteractionRepository;
        this.medicineInteractionRepository = medicineInteractionRepository;
    }

    @Data
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        private JsonNode planJson;
        private List<AlternativeInfo> alternatives;
    }

    @Data
    public static class AlternativeInfo {
        private String originalDrug;
        private String alternativeDrug;
        private String dose;
        private String frequency;
        private String route;
        private String reason;
    }

    public ValidationResult validate(String aiResponse, PatientContext context) {
        log.info("开始验证AI返回结果（完整业务校验）");
        ValidationResult result = new ValidationResult();
        result.setValid(false);
        result.setErrors(new ArrayList<>());
        result.setWarnings(new ArrayList<>());

        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            result.getErrors().add("AI返回为空");
            return result;
        }

        try {
            int jsonStart = aiResponse.indexOf('{');
            int jsonEnd = aiResponse.lastIndexOf('}');
            if (jsonStart == -1 || jsonEnd == -1) {
                result.getErrors().add("AI返回不是有效的JSON格式");
                return result;
            }

            String jsonStr = aiResponse.substring(jsonStart, jsonEnd + 1);
            JsonNode root = objectMapper.readTree(jsonStr);
            result.setPlanJson(root);

            if (!root.has("plan") || !root.get("plan").isArray()) {
                result.getErrors().add("缺少 plan 数组");
                return result;
            }

            JsonNode planArray = root.get("plan");
            if (planArray.size() == 0) {
                result.getErrors().add("plan 数组为空");
                return result;
            }

            Set<String> medicineNames = new HashSet<>();
            List<String> medicineNamesOrdered = new ArrayList<>();

            for (int i = 0; i < planArray.size(); i++) {
                JsonNode item = planArray.get(i);
                String prefix = "plan[" + i + "]";
                validateTime(item, prefix, result);
                String medicineName = validateMedicine(item, prefix, context, result);
                if (medicineName != null) { medicineNames.add(medicineName); medicineNamesOrdered.add(medicineName); }
                validateDose(item, prefix, result);
                validateRemark(item, prefix, result);
            }

            validateDrugConflicts(medicineNamesOrdered, result);
            validateDuplicates(planArray, result);

            if (!root.has("warnings")) log.warn("缺少 warnings 字段，将自动添加空数组");

            parseAlternatives(root, result);

            if (result.getErrors().isEmpty()) {
                result.setValid(true);
                log.info("AI返回结果验证通过，警告数={}", result.getWarnings().size());
            } else log.warn("AI返回结果验证失败，错误数={}", result.getErrors().size());

        } catch (Exception e) {
            log.error("验证AI返回结果时发生异常", e);
            result.getErrors().add("解析JSON失败: " + e.getMessage());
        }
        return result;
    }

    private void validateTime(JsonNode item, String prefix, ValidationResult result) {
        if (!item.has("time") || item.get("time").isNull() || item.get("time").asText().isEmpty()) {
            result.getErrors().add(prefix + ".time 不能为空"); return;
        }
        try { TIME_FORMATTER.parse(item.get("time").asText().trim()); }
        catch (DateTimeParseException e) { result.getErrors().add(prefix + ".time 格式错误，应为 HH:mm"); }
    }

    private String validateMedicine(JsonNode item, String prefix, PatientContext context, ValidationResult result) {
        if (!item.has("medicine") || item.get("medicine").isNull() || item.get("medicine").asText().isEmpty()) {
            result.getErrors().add(prefix + ".medicine 不能为空"); return null;
        }
        String medicineName = item.get("medicine").asText().trim();
        if (!medicineExists(medicineName)) log.info("药品 \"{}\" 不存在于数据库，将自动创建", medicineName);
        if (context != null && isAllergyMedicine(context, medicineName))
            result.getErrors().add(prefix + ".medicine \"" + medicineName + "\" 可能引发过敏反应，患者存在相关过敏史");
        return medicineName;
    }

    private void validateDose(JsonNode item, String prefix, ValidationResult result) {
        if (!item.has("dose") || item.get("dose").isNull() || item.get("dose").asText().isEmpty())
            result.getErrors().add(prefix + ".dose 不能为空");
    }

    private void validateRemark(JsonNode item, String prefix, ValidationResult result) {
        if (!item.has("remark") || item.get("remark").isNull() || item.get("remark").asText().isEmpty())
            result.getErrors().add(prefix + ".remark 不能为空");
    }

    private void validateDrugConflicts(List<String> medicineNames, ValidationResult result) {
        if (medicineNames.size() < 2) return;
        for (int i = 0; i < medicineNames.size(); i++)
            for (int j = i + 1; j < medicineNames.size(); j++)
                if (checkConflict(medicineNames.get(i), medicineNames.get(j)))
                    result.getWarnings().add("药物冲突检测: " + medicineNames.get(i) + " + " + medicineNames.get(j) + " 存在相互作用风险");
    }

    private void validateDuplicates(JsonNode planArray, ValidationResult result) {
        Set<String> uniqueKeys = new HashSet<>();
        for (int i = 0; i < planArray.size(); i++) {
            JsonNode item = planArray.get(i);
            String key = (item.has("time") ? item.get("time").asText().trim() : "") + "-" + (item.has("medicine") ? item.get("medicine").asText().trim() : "");
            if (uniqueKeys.contains(key)) result.getWarnings().add("plan[" + i + "] 存在重复: " + key);
            else uniqueKeys.add(key);
        }
    }

    private boolean medicineExists(String medicineName) {
        try { return !medicineRepository.findByMedicineName(medicineName).isEmpty(); }
        catch (Exception e) { log.warn("查询药品失败: {}", medicineName, e); return true; }
    }

    private boolean isAllergyMedicine(PatientContext context, String medicineName) {
        if (context.getAllergyTags() == null) return false;
        for (com.medical.service.ai.context.RiskTag allergyTag : context.getAllergyTags()) {
            String affectsCategory = allergyTag.getAffectsDrugCategory();
            if (affectsCategory == null) continue;
            if (affectsCategory.contains("青霉素") && medicineName.contains("青霉素")) return true;
            if (affectsCategory.contains("头孢") && medicineName.contains("头孢")) return true;
            if (affectsCategory.contains("磺胺") && medicineName.contains("磺胺")) return true;
            if (affectsCategory.contains("阿司匹林") && medicineName.contains("阿司匹林")) return true;
            if (affectsCategory.contains("氨基糖苷") && (medicineName.contains("链霉素") || medicineName.contains("庆大霉素") || medicineName.contains("阿米卡星"))) return true;
        }
        return false;
    }

    private boolean checkConflict(String nameA, String nameB) {
        Medicine medicineA = medicineRepository.findByMedicineName(nameA).stream().findFirst().orElse(null);
        Medicine medicineB = medicineRepository.findByMedicineName(nameB).stream().findFirst().orElse(null);
        if (medicineA != null && medicineB != null) {
            if (!medicineInteractionRepository.findByMedicineIdAAndMedicineIdB(medicineA.getMedicineId(), medicineB.getMedicineId()).isEmpty()) return true;
            if (!medicineInteractionRepository.findByMedicineIdAAndMedicineIdB(medicineB.getMedicineId(), medicineA.getMedicineId()).isEmpty()) return true;
        }
        if (!drugInteractionRepository.findByDrugNameAAndDrugNameB(nameA, nameB).isEmpty()) return true;
        if (!drugInteractionRepository.findByDrugNameAAndDrugNameB(nameB, nameA).isEmpty()) return true;
        return false;
    }

    private void parseAlternatives(JsonNode root, ValidationResult result) {
        if (!root.has("alternatives") || root.get("alternatives").isNull()) { log.info("AI未返回替代药物方案"); return; }
        JsonNode alternativesNode = root.get("alternatives");
        if (!alternativesNode.isArray()) { log.warn("alternatives字段不是数组格式"); return; }
        List<AlternativeInfo> alternatives = new ArrayList<>();
        for (int i = 0; i < alternativesNode.size(); i++) {
            JsonNode altNode = alternativesNode.get(i);
            AlternativeInfo alt = new AlternativeInfo();
            alt.setOriginalDrug(altNode.has("originalDrug") && !altNode.get("originalDrug").isNull() ? altNode.get("originalDrug").asText().trim() : null);
            alt.setAlternativeDrug(altNode.has("alternativeDrug") && !altNode.get("alternativeDrug").isNull() ? altNode.get("alternativeDrug").asText().trim() : null);
            alt.setDose(altNode.has("dose") && !altNode.get("dose").isNull() ? altNode.get("dose").asText().trim() : null);
            alt.setFrequency(altNode.has("frequency") && !altNode.get("frequency").isNull() ? altNode.get("frequency").asText().trim() : null);
            alt.setRoute(altNode.has("route") && !altNode.get("route").isNull() ? altNode.get("route").asText().trim() : null);
            alt.setReason(altNode.has("reason") && !altNode.get("reason").isNull() ? altNode.get("reason").asText().trim() : null);
            if (alt.getOriginalDrug() == null || alt.getAlternativeDrug() == null) { log.warn("替代方案[{}]缺少必要字段", i); continue; }
            alternatives.add(alt);
            log.info("解析到替代方案: {} → {}", alt.getOriginalDrug(), alt.getAlternativeDrug());
        }
        result.setAlternatives(alternatives);
        if (!alternatives.isEmpty()) result.getWarnings().add("AI提供了 " + alternatives.size() + " 个替代药物方案，将在计划中体现");
    }
}
