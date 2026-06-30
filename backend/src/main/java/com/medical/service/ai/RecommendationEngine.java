package com.medical.service.ai;

import com.medical.service.ai.context.PatientContext;
import com.medical.service.ai.context.RiskTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RecommendationEngine {

    public List<Recommendation> generateRecommendations(PatientContext context, TreatmentContext treatment, RuleResult ruleResult) {
        log.info("开始生成建议，风险等级={}", ruleResult.getRiskLevel());
        List<Recommendation> recommendations = new ArrayList<>();
        addRiskLevelRecommendations(ruleResult, recommendations);
        addConflictRecommendations(ruleResult, recommendations);
        addDiseaseRecommendations(context, treatment, recommendations);
        addAllergyRecommendations(context, treatment, recommendations);
        addAdviceRecommendations(treatment, recommendations);
        addFollowUpRecommendations(treatment, recommendations);
        log.info("建议生成完成，共生成 {} 条建议", recommendations.size());
        return recommendations;
    }

    private void addRiskLevelRecommendations(RuleResult ruleResult, List<Recommendation> recommendations) {
        Recommendation.RecommendationType type;
        Recommendation.Priority priority;
        String title, content;
        switch (ruleResult.getRiskLevel()) {
            case CRITICAL -> { type = Recommendation.RecommendationType.REQUIREMENT; priority = Recommendation.Priority.CRITICAL; title = "高风险警告"; content = "当前用药方案存在高风险，建议立即咨询医生调整用药方案"; }
            case HIGH -> { type = Recommendation.RecommendationType.WARNING; priority = Recommendation.Priority.HIGH; title = "风险提示"; content = "当前用药方案存在中等风险，建议在医生指导下调整用药"; }
            case MEDIUM -> { type = Recommendation.RecommendationType.ADVICE; priority = Recommendation.Priority.MEDIUM; title = "用药建议"; content = "当前用药方案存在轻微风险，建议注意观察用药反应"; }
            default -> { type = Recommendation.RecommendationType.SUGGESTION; priority = Recommendation.Priority.LOW; title = "用药提示"; content = "当前用药方案风险较低，建议按医嘱正常服药"; }
        }
        recommendations.add(createRecommendation(type, priority, title, content, null, "风险等级评估"));
    }

    private void addConflictRecommendations(RuleResult ruleResult, List<Recommendation> recommendations) {
        if (!ruleResult.isHasConflict() || ruleResult.getConflicts() == null) return;
        for (RuleEngine.ConflictInfo conflict : ruleResult.getConflicts()) {
            String title = "药物相互作用警告";
            String content = conflict.getDrugA() + "与" + conflict.getDrugB() + "存在" + conflict.getLevel() + "相互作用";
            if (conflict.getDescription() != null && !conflict.getDescription().isEmpty()) content += "，" + conflict.getDescription();
            if (conflict.getSuggestion() != null && !conflict.getSuggestion().isEmpty()) content += "。建议：" + conflict.getSuggestion();
            else content += "。建议咨询医生调整用药间隔或更换药物";
            Recommendation.Priority priority = switch (conflict.getLevel().toUpperCase()) {
                case "CRITICAL", "危急", "严重" -> Recommendation.Priority.CRITICAL;
                case "HIGH", "高" -> Recommendation.Priority.HIGH;
                default -> Recommendation.Priority.MEDIUM;
            };
            recommendations.add(createRecommendation(Recommendation.RecommendationType.WARNING, priority, title, content, conflict.getDrugA() + ", " + conflict.getDrugB(), "药物冲突检测"));
        }
    }

    private void addDiseaseRecommendations(PatientContext context, TreatmentContext treatment, List<Recommendation> recommendations) {
        if (context.getDiseaseRiskTags() == null || context.getDiseaseRiskTags().isEmpty()) return;
        for (RiskTag tag : context.getDiseaseRiskTags()) {
            String title = "病史风险提示";
            String content = "患者有" + tag.getName() + "病史，";
            content += tag.getAffectsDrugCategory() != null ? "建议避免使用" + tag.getAffectsDrugCategory() : "建议在医生指导下用药";
            Recommendation.Priority priority = switch (tag.getLevel()) {
                case CRITICAL -> Recommendation.Priority.CRITICAL; case HIGH -> Recommendation.Priority.HIGH;
                default -> Recommendation.Priority.MEDIUM;
            };
            recommendations.add(createRecommendation(Recommendation.RecommendationType.ADVICE, priority, title, content, null, "病史风险评估"));
        }
    }

    private void addAllergyRecommendations(PatientContext context, TreatmentContext treatment, List<Recommendation> recommendations) {
        if (context.getAllergyTags() == null || context.getAllergyTags().isEmpty()) return;
        for (RiskTag allergyTag : context.getAllergyTags()) {
            String title = "过敏风险警告";
            String content = "患者对" + allergyTag.getName() + "过敏，";
            content += allergyTag.getAffectsDrugCategory() != null ? "严禁使用" + allergyTag.getAffectsDrugCategory() : "请避免使用相关药物";
            if (treatment.getMedicines() != null) {
                for (TreatmentContext.MedicineItem medicine : treatment.getMedicines()) {
                    if (isAllergyRisk(allergyTag, medicine)) content += "。当前用药" + medicine.getName() + "可能引发过敏反应，建议立即更换";
                }
            }
            recommendations.add(createRecommendation(Recommendation.RecommendationType.REQUIREMENT, Recommendation.Priority.CRITICAL, title, content, allergyTag.getAffectsDrugCategory(), "过敏风险评估"));
        }
    }

    private void addAdviceRecommendations(TreatmentContext treatment, List<Recommendation> recommendations) {
        if (treatment.getAdvices() == null || treatment.getAdvices().isEmpty()) return;
        for (String advice : treatment.getAdvices()) {
            recommendations.add(createRecommendation(Recommendation.RecommendationType.ADVICE, Recommendation.Priority.MEDIUM, "医嘱提示", advice, null, "病历医嘱"));
        }
    }

    private void addFollowUpRecommendations(TreatmentContext treatment, List<Recommendation> recommendations) {
        if (treatment.getFollowUps() == null || treatment.getFollowUps().isEmpty()) return;
        for (String followUp : treatment.getFollowUps()) {
            recommendations.add(createRecommendation(Recommendation.RecommendationType.SUGGESTION, Recommendation.Priority.LOW, "随访建议", followUp, null, "病历随访"));
        }
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

    private Recommendation createRecommendation(Recommendation.RecommendationType type, Recommendation.Priority priority, String title, String content, String medicineName, String sourceRule) {
        Recommendation recommendation = new Recommendation();
        recommendation.setType(type); recommendation.setPriority(priority);
        recommendation.setTitle(title); recommendation.setContent(content);
        recommendation.setMedicineName(medicineName); recommendation.setSourceRule(sourceRule);
        return recommendation;
    }
}
