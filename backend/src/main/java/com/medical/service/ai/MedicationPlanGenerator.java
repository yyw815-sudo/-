package com.medical.service.ai;

import com.medical.entity.Medicine;
import com.medical.entity.MedicationDailyRecord;
import com.medical.entity.MedicationPlan;
import com.medical.repository.MedicineRepository;
import com.medical.repository.MedicationDailyRecordRepository;
import com.medical.repository.MedicationPlanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class MedicationPlanGenerator {

    private final MedicationPlanRepository planRepository;
    private final MedicationDailyRecordRepository dailyRecordRepository;
    private final MedicineRepository medicineRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public MedicationPlanGenerator(MedicationPlanRepository planRepository, MedicationDailyRecordRepository dailyRecordRepository, MedicineRepository medicineRepository) {
        this.planRepository = planRepository;
        this.dailyRecordRepository = dailyRecordRepository;
        this.medicineRepository = medicineRepository;
    }

    @Data
    public static class GeneratedResult {
        private List<MedicationPlan> plans;
        private List<String> warnings;
        private Boolean isExisting;
        private List<String> alternativeInfo;
    }

    @Transactional
    public GeneratedResult generate(Long userId, Long recordId, JsonNode validatedJson, List<PlanValidator.AlternativeInfo> alternatives) {
        log.info("开始生成用药计划，userId={}, recordId={}, 替代方案数={}", userId, recordId, alternatives != null ? alternatives.size() : 0);
        GeneratedResult result = new GeneratedResult();
        result.setPlans(new ArrayList<>());
        result.setWarnings(new ArrayList<>());
        result.setIsExisting(false);
        result.setAlternativeInfo(new ArrayList<>());

        List<MedicationPlan> existingActivePlans = planRepository.findByUserIdAndRecordId(userId, recordId).stream().filter(p -> p.getStatus() == 1).toList();
        if (!existingActivePlans.isEmpty()) {
            log.info("该病历已有进行中的用药计划，跳过重复分析，计划数: {}", existingActivePlans.size());
            result.setPlans(new ArrayList<>(existingActivePlans));
            result.setIsExisting(true);
            return result;
        }

        JsonNode planArray = validatedJson.get("plan");
        JsonNode warningsNode = validatedJson.has("warnings") ? validatedJson.get("warnings") : null;
        if (warningsNode != null && warningsNode.isArray()) { for (JsonNode w : warningsNode) result.getWarnings().add(w.asText()); }

        List<String> processedMedicines = new ArrayList<>();
        Map<String, PlanValidator.AlternativeInfo> alternativeMap = new HashMap<>();
        if (alternatives != null) { for (PlanValidator.AlternativeInfo alt : alternatives) alternativeMap.put(alt.getOriginalDrug(), alt); }

        for (JsonNode item : planArray) {
            String medicineName = item.get("medicine").asText().trim();
            String dose = item.get("dose").asText().trim();
            String time = item.get("time").asText().trim();
            String remark = item.get("remark").asText().trim();

            boolean isAlternative = false;
            String originalDrug = null;
            for (PlanValidator.AlternativeInfo alt : alternativeMap.values()) {
                if (alt.getAlternativeDrug().equals(medicineName)) { isAlternative = true; originalDrug = alt.getOriginalDrug(); break; }
            }

            if (processedMedicines.contains(medicineName)) { log.info("跳过重复药品: {}", medicineName); continue; }
            processedMedicines.add(medicineName);

            Long medicineId = findOrCreateMedicine(medicineName);
            MedicationPlan plan = new MedicationPlan();
            plan.setUserId(userId); plan.setRecordId(recordId); plan.setMedicineId(medicineId);
            plan.setDosage(dose); plan.setFrequency(extractFrequency(remark)); plan.setTimesPerDay(1);
            plan.setStartDate(LocalDate.now()); plan.setEndDate(LocalDate.now().plusDays(30));
            plan.setPurpose(remark); plan.setStatus(1);

            if (isAlternative && originalDrug != null) {
                PlanValidator.AlternativeInfo alt = alternativeMap.get(originalDrug);
                if (alt != null && alt.getReason() != null) {
                    plan.setPurpose("[替代" + originalDrug + "] " + alt.getReason() + " | " + remark);
                    result.getAlternativeInfo().add(medicineName + " 替代了 " + originalDrug + "（" + alt.getReason() + "）");
                }
            }
            result.getPlans().add(plan);
        }

        if (alternatives != null) {
            for (PlanValidator.AlternativeInfo alt : alternatives) {
                if (processedMedicines.contains(alt.getOriginalDrug())) continue;
                if (processedMedicines.contains(alt.getAlternativeDrug())) continue;
                log.info("处理plan外的替代药物: {} → {}", alt.getOriginalDrug(), alt.getAlternativeDrug());
                Long medicineId = findOrCreateMedicine(alt.getAlternativeDrug());
                MedicationPlan plan = new MedicationPlan();
                plan.setUserId(userId); plan.setRecordId(recordId); plan.setMedicineId(medicineId);
                plan.setDosage(alt.getDose() != null ? alt.getDose() : "常规剂量");
                plan.setFrequency(alt.getFrequency() != null ? alt.getFrequency() : "每日1次");
                plan.setTimesPerDay(1);
                plan.setStartDate(LocalDate.now()); plan.setEndDate(LocalDate.now().plusDays(30));
                plan.setPurpose("[替代" + alt.getOriginalDrug() + "] " + (alt.getReason() != null ? alt.getReason() : "无冲突替代方案"));
                plan.setStatus(1);
                result.getPlans().add(plan);
                processedMedicines.add(alt.getAlternativeDrug());
                result.getAlternativeInfo().add(alt.getAlternativeDrug() + " 替代了 " + alt.getOriginalDrug() + "（" + alt.getReason() + "）");
            }
        }

        List<MedicationPlan> savedPlans = planRepository.saveAll(result.getPlans());
        for (MedicationPlan plan : savedPlans) generateDailyRecords(plan);

        log.info("用药计划生成完成，共生成 {} 个计划", savedPlans.size());
        result.setPlans(savedPlans);
        return result;
    }

    @Transactional
    public GeneratedResult generate(Long userId, Long recordId, JsonNode validatedJson) {
        return generate(userId, recordId, validatedJson, null);
    }

    private Long findOrCreateMedicine(String medicineName) {
        Medicine existing = medicineRepository.findByMedicineName(medicineName).stream().findFirst().orElse(null);
        if (existing != null) return existing.getMedicineId();
        log.info("药品不存在，自动创建: {}", medicineName);
        Medicine medicine = new Medicine(); medicine.setMedicineName(medicineName);
        return medicineRepository.save(medicine).getMedicineId();
    }

    private String extractFrequency(String remark) {
        if (remark == null) return "每日一次";
        if (remark.contains("每日两次") || remark.contains("每天两次") || remark.contains("每日2次")) return "每日两次";
        else if (remark.contains("每日三次") || remark.contains("每天三次") || remark.contains("每日3次")) return "每日三次";
        else if (remark.contains("每日一次") || remark.contains("每天一次") || remark.contains("每日1次")) return "每日一次";
        return "每日一次";
    }

    private void generateDailyRecords(MedicationPlan plan) {
        LocalDate start = plan.getStartDate();
        LocalDate end = plan.getEndDate() != null ? plan.getEndDate() : start.plusDays(30);
        int timesPerDay = plan.getTimesPerDay() != null && plan.getTimesPerDay() > 0 ? plan.getTimesPerDay() : 1;
        int intervalHours = plan.getIntervalHours() != null && plan.getIntervalHours() > 0 ? plan.getIntervalHours() : (24 / timesPerDay);
        List<MedicationDailyRecord> records = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            for (int i = 0; i < timesPerDay; i++) {
                LocalTime time = LocalTime.of(8 + (i * intervalHours), 0);
                if (time.getHour() >= 24) break;
                MedicationDailyRecord record = new MedicationDailyRecord();
                record.setPlanId(plan.getPlanId()); record.setUserId(plan.getUserId());
                record.setMedicineId(plan.getMedicineId());
                record.setScheduledTime(LocalDateTime.of(current, time));
                record.setStatus(0);
                records.add(record);
            }
            current = current.plusDays(1);
        }
        dailyRecordRepository.saveAll(records);
    }
}
