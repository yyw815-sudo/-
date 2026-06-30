package com.medical.service.ai;

import com.medical.entity.DrugInteraction;
import com.medical.entity.Medicine;
import com.medical.entity.MedicineInteraction;
import com.medical.repository.DrugInteractionRepository;
import com.medical.repository.MedicineInteractionRepository;
import com.medical.repository.MedicineRepository;
import com.medical.service.ai.context.PatientContext;
import com.medical.service.ai.context.RiskTag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class RuleEngine {

    private final MedicineRepository medicineRepository;
    private final DrugInteractionRepository drugInteractionRepository;
    private final MedicineInteractionRepository medicineInteractionRepository;

    public RuleEngine(MedicineRepository medicineRepository,
                     DrugInteractionRepository drugInteractionRepository,
                     MedicineInteractionRepository medicineInteractionRepository) {
        this.medicineRepository = medicineRepository;
        this.drugInteractionRepository = drugInteractionRepository;
        this.medicineInteractionRepository = medicineInteractionRepository;
    }

    @Data
    public static class ConflictInfo {
        private String drugA;
        private String drugB;
        private String level;
        private String description;
        private String suggestion;
    }

    public RuleResult execute(PatientContext context, TreatmentContext treatment) {
        log.info("开始规则引擎检测");
        RuleResult result = new RuleResult();
        detectDrugConflicts(treatment.getMedicines(), result);
        evaluateDiseaseRisks(context, result);
        evaluateAllergyRisks(context, treatment.getMedicines(), result);
        result.setRiskLevel(RuleResult.RiskLevel.fromScore(result.getRiskScore()));
        log.info("规则引擎检测完成，风险评分={}，风险等级={}", result.getRiskScore(), result.getRiskLevel());
        return result;
    }

    private void detectDrugConflicts(List<TreatmentContext.MedicineItem> medicines, RuleResult result) {
        if (medicines == null || medicines.size() < 2) return;
        List<String> medicineNames = medicines.stream().map(TreatmentContext.MedicineItem::getName).toList();
        for (int i = 0; i < medicineNames.size(); i++) {
            for (int j = i + 1; j < medicineNames.size(); j++) {
                ConflictInfo conflict = checkConflict(medicineNames.get(i), medicineNames.get(j));
                if (conflict != null) {
                    result.addConflict(conflict);
                    result.addWarning("药物冲突: " + medicineNames.get(i) + " + " + medicineNames.get(j) + " = " + conflict.getLevel());
                    int score = calculateConflictScore(conflict.getLevel());
                    result.addRiskScore(score);
                    log.warn("检测到药物冲突（+{}分）: {} + {}", score, medicineNames.get(i), medicineNames.get(j));
                }
            }
        }
    }

    private void evaluateDiseaseRisks(PatientContext context, RuleResult result) {
        if (context.getDiseaseRiskTags() == null || context.getDiseaseRiskTags().isEmpty()) return;
        result.setHasDiseaseRisk(true);
        for (RiskTag tag : context.getDiseaseRiskTags()) {
            result.addRiskTag(tag.getName() + "(" + tag.getLevel() + ")");
            int score = calculateDiseaseScore(tag.getLevel());
            result.addRiskScore(score);
            result.addWarning("病史风险: " + tag.getName() + "(" + tag.getLevel() + ")");
            log.info("检测到病史风险（+{}分）: {}", score, tag.getName());
        }
    }

    private void evaluateAllergyRisks(PatientContext context, List<TreatmentContext.MedicineItem> medicines, RuleResult result) {
        if (context.getAllergyTags() == null || context.getAllergyTags().isEmpty()) return;
        result.setHasAllergyRisk(true);
        for (RiskTag allergyTag : context.getAllergyTags()) {
            result.addRiskTag(allergyTag.getName() + "(" + allergyTag.getLevel() + ")");
            int score = calculateAllergyScore(allergyTag.getLevel());
            result.addRiskScore(score);
            result.addWarning("过敏风险: " + allergyTag.getName());
            if (medicines != null) {
                for (TreatmentContext.MedicineItem medicine : medicines) {
                    if (isAllergyRisk(allergyTag, medicine)) {
                        result.addWarning("严重警告: 当前用药 " + medicine.getName() + " 可能引发过敏反应");
                        result.addRiskScore(30);
                    }
                }
            }
            log.warn("检测到过敏风险（+{}分）: {}", score, allergyTag.getName());
        }
    }

    private ConflictInfo checkConflict(String nameA, String nameB) {
        Medicine medicineA = medicineRepository.findByMedicineName(nameA).stream().findFirst().orElse(null);
        Medicine medicineB = medicineRepository.findByMedicineName(nameB).stream().findFirst().orElse(null);
        if (medicineA != null && medicineB != null) {
            List<MedicineInteraction> interactions = medicineInteractionRepository
                    .findByMedicineIdAAndMedicineIdB(medicineA.getMedicineId(), medicineB.getMedicineId());
            if (!interactions.isEmpty()) {
                MedicineInteraction mi = interactions.get(0);
                ConflictInfo ci = new ConflictInfo();
                ci.setDrugA(nameA); ci.setDrugB(nameB);
                ci.setLevel(mi.getSeverity() != null ? mi.getSeverity() : "MEDIUM");
                ci.setDescription(mi.getDescription());
                ci.setSuggestion(suggestAlternativeDrug(medicineA, medicineB));
                return ci;
            }
            interactions = medicineInteractionRepository
                    .findByMedicineIdAAndMedicineIdB(medicineB.getMedicineId(), medicineA.getMedicineId());
            if (!interactions.isEmpty()) {
                MedicineInteraction mi = interactions.get(0);
                ConflictInfo ci = new ConflictInfo();
                ci.setDrugA(nameA); ci.setDrugB(nameB);
                ci.setLevel(mi.getSeverity() != null ? mi.getSeverity() : "MEDIUM");
                ci.setDescription(mi.getDescription());
                ci.setSuggestion(suggestAlternativeDrug(medicineB, medicineA));
                return ci;
            }
        }
        List<DrugInteraction> drugInteractions = drugInteractionRepository.findByDrugNameAAndDrugNameB(nameA, nameB);
        if (!drugInteractions.isEmpty()) {
            DrugInteraction di = drugInteractions.get(0);
            ConflictInfo ci = new ConflictInfo();
            ci.setDrugA(nameA); ci.setDrugB(nameB);
            ci.setLevel(di.getLevel() != null ? di.getLevel() : "MEDIUM");
            ci.setDescription(di.getDescription());
            ci.setSuggestion(di.getSuggestion());
            return ci;
        }
        drugInteractions = drugInteractionRepository.findByDrugNameAAndDrugNameB(nameB, nameA);
        if (!drugInteractions.isEmpty()) {
            DrugInteraction di = drugInteractions.get(0);
            ConflictInfo ci = new ConflictInfo();
            ci.setDrugA(nameA); ci.setDrugB(nameB);
            ci.setLevel(di.getLevel() != null ? di.getLevel() : "MEDIUM");
            ci.setDescription(di.getDescription());
            ci.setSuggestion(di.getSuggestion());
            return ci;
        }
        return null;
    }

    /**
     * 为冲突药品查找替代药品建议（基于适应症关键词匹配）
     */
    private String suggestAlternativeDrug(Medicine drugA, Medicine drugB) {
        if (drugA.getIndication() != null && !drugA.getIndication().isEmpty()) {
            String[] keywords = drugA.getIndication().split("[，,、；;\\s]+");
            for (String keyword : keywords) {
                if (keyword.length() < 2) continue;
                List<Medicine> candidates = medicineRepository.findByIndicationContaining(keyword);
                for (Medicine candidate : candidates) {
                    if (candidate.getMedicineId().equals(drugA.getMedicineId())) continue;
                    if (candidate.getMedicineId().equals(drugB.getMedicineId())) continue;
                    if (!hasMedicineInteractionConflict(candidate, drugB)) {
                        return "建议将" + drugA.getMedicineName() + "替换为" + candidate.getMedicineName() + "（适应症相似，无明显冲突）";
                    }
                }
            }
        }
        if (drugB.getIndication() != null && !drugB.getIndication().isEmpty()) {
            String[] keywords = drugB.getIndication().split("[，,、；;\\s]+");
            for (String keyword : keywords) {
                if (keyword.length() < 2) continue;
                List<Medicine> candidates = medicineRepository.findByIndicationContaining(keyword);
                for (Medicine candidate : candidates) {
                    if (candidate.getMedicineId().equals(drugA.getMedicineId())) continue;
                    if (candidate.getMedicineId().equals(drugB.getMedicineId())) continue;
                    if (!hasMedicineInteractionConflict(candidate, drugA)) {
                        return "建议将" + drugB.getMedicineName() + "替换为" + candidate.getMedicineName() + "（适应症相似，无明显冲突）";
                    }
                }
            }
        }
        return "建议咨询医生，考虑调整用药方案或改用替代药物";
    }

    private boolean hasMedicineInteractionConflict(Medicine m1, Medicine m2) {
        if (!medicineInteractionRepository.findByMedicineIdAAndMedicineIdB(m1.getMedicineId(), m2.getMedicineId()).isEmpty()) return true;
        if (!medicineInteractionRepository.findByMedicineIdAAndMedicineIdB(m2.getMedicineId(), m1.getMedicineId()).isEmpty()) return true;
        return false;
    }

    private boolean isAllergyRisk(RiskTag allergyTag, TreatmentContext.MedicineItem medicine) {
        String affectsCategory = allergyTag.getAffectsDrugCategory();
        String medicineName = medicine.getName();
        if (affectsCategory == null || medicineName == null) return false;
        return (affectsCategory.contains("青霉素") && medicineName.contains("青霉素")) ||
               (affectsCategory.contains("头孢") && medicineName.contains("头孢")) ||
               (affectsCategory.contains("磺胺") && medicineName.contains("磺胺")) ||
               (affectsCategory.contains("阿司匹林") && medicineName.contains("阿司匹林")) ||
               (affectsCategory.contains("氨基糖苷") && (medicineName.contains("链霉素") || medicineName.contains("庆大霉素") || medicineName.contains("阿米卡星")));
    }

    private int calculateConflictScore(String level) {
        if (level == null) return 10;
        return switch (level.toUpperCase()) {
            case "CRITICAL", "危急", "严重" -> 30;
            case "HIGH", "高" -> 20;
            default -> 10;
        };
    }

    private int calculateDiseaseScore(RiskTag.RiskLevel level) {
        return switch (level) {
            case CRITICAL -> 25; case HIGH -> 15; case MEDIUM -> 8; default -> 3;
        };
    }

    private int calculateAllergyScore(RiskTag.RiskLevel level) {
        return switch (level) {
            case CRITICAL -> 30; case HIGH -> 20; case MEDIUM -> 10; default -> 5;
        };
    }

    /**
     * 跨病历冲突检测 - 已废弃，请使用 MedicationPlanService.checkCrossRecordConflicts
     */
    @Deprecated
    public ConflictCheckResult checkCrossRecordConflicts(List<Long> recordIds) {
        log.warn("checkCrossRecordConflicts 已废弃，请使用 MedicationPlanService.checkCrossRecordConflicts");
        ConflictCheckResult result = new ConflictCheckResult();
        result.setRecordCount(recordIds.size());
        result.setAllMedicines(new java.util.ArrayList<>());
        result.setHasConflict(false);
        result.setConflicts(new java.util.ArrayList<>());
        return result;
    }

    @lombok.Data
    public static class ConflictCheckResult {
        private int recordCount;
        private int totalDrugs;
        private List<String> allMedicines;
        private boolean hasConflict;
        private List<com.medical.dto.ConflictCheckResultDTO.ConflictItem> conflicts;
    }
}
