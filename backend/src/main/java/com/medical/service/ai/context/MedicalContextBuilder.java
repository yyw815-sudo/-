package com.medical.service.ai.context;

import com.medical.entity.MedicalRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class MedicalContextBuilder {

    private static final Map<String, DiseaseRiskInfo> DISEASE_RISK_MAP = new LinkedHashMap<>();
    private static final Map<String, AllergyInfo> ALLERGY_RISK_MAP = new LinkedHashMap<>();

    static {
        DISEASE_RISK_MAP.put("高血压", new DiseaseRiskInfo(RiskTag.RiskLevel.MEDIUM, "钙通道阻滞剂、ACEI类"));
        DISEASE_RISK_MAP.put("糖尿病", new DiseaseRiskInfo(RiskTag.RiskLevel.MEDIUM, "糖皮质激素类"));
        DISEASE_RISK_MAP.put("胃溃疡", new DiseaseRiskInfo(RiskTag.RiskLevel.HIGH, "NSAID类"));
        DISEASE_RISK_MAP.put("肾功能不全", new DiseaseRiskInfo(RiskTag.RiskLevel.HIGH, "经肾代谢药物"));
        DISEASE_RISK_MAP.put("肝功能异常", new DiseaseRiskInfo(RiskTag.RiskLevel.HIGH, "经肝代谢药物"));
        DISEASE_RISK_MAP.put("冠心病", new DiseaseRiskInfo(RiskTag.RiskLevel.HIGH, "某些降压药"));
        DISEASE_RISK_MAP.put("哮喘", new DiseaseRiskInfo(RiskTag.RiskLevel.MEDIUM, "β受体阻滞剂"));
        DISEASE_RISK_MAP.put("心力衰竭", new DiseaseRiskInfo(RiskTag.RiskLevel.CRITICAL, "某些抗炎药"));
        DISEASE_RISK_MAP.put("甲状腺功能亢进", new DiseaseRiskInfo(RiskTag.RiskLevel.MEDIUM, "含碘药物"));
        DISEASE_RISK_MAP.put("癫痫", new DiseaseRiskInfo(RiskTag.RiskLevel.HIGH, "喹诺酮类抗生素"));

        ALLERGY_RISK_MAP.put("青霉素过敏", new AllergyInfo(RiskTag.RiskLevel.CRITICAL, "青霉素类、头孢类"));
        ALLERGY_RISK_MAP.put("头孢过敏", new AllergyInfo(RiskTag.RiskLevel.CRITICAL, "头孢类"));
        ALLERGY_RISK_MAP.put("磺胺过敏", new AllergyInfo(RiskTag.RiskLevel.CRITICAL, "磺胺类"));
        ALLERGY_RISK_MAP.put("阿司匹林过敏", new AllergyInfo(RiskTag.RiskLevel.HIGH, "阿司匹林、NSAID类"));
        ALLERGY_RISK_MAP.put("酒精过敏", new AllergyInfo(RiskTag.RiskLevel.MEDIUM, "含酒精成分药物"));
        ALLERGY_RISK_MAP.put("链霉素过敏", new AllergyInfo(RiskTag.RiskLevel.CRITICAL, "氨基糖苷类"));
        ALLERGY_RISK_MAP.put("庆大霉素过敏", new AllergyInfo(RiskTag.RiskLevel.CRITICAL, "氨基糖苷类"));
        ALLERGY_RISK_MAP.put("阿米卡星过敏", new AllergyInfo(RiskTag.RiskLevel.CRITICAL, "氨基糖苷类"));
    }

    public PatientContext buildContext(MedicalRecord record) {
        log.info("构建医疗上下文，recordId={}", record.getRecordId());
        PatientContext context = new PatientContext();

        if (record.getAge() != null) {
            context.setAge(record.getAge());
        }
        if (record.getGender() != null) {
            context.setGender(record.getGender());
        }

        // 提取诊断
        List<String> diagnoses = extractDiagnoses(record);
        context.setDiagnoses(diagnoses);

        // 药品信息
        context.setMedicines(record.getMedicines());

        // 现病史和既往史
        context.setPresentHistory(record.getPresentHistory());
        context.setPastHistory(record.getPastHistory());

        // 提取疾病风险（从诊断+既往史）
        extractDiseaseRisks(diagnoses, context);
        if (record.getPastHistory() != null && !record.getPastHistory().isEmpty()) {
            extractDiseaseRisksFromPastHistory(record.getPastHistory(), context);
        }

        // 提取处理意见中的风险
        String treatment = record.getTreatment();
        if (treatment != null && !treatment.isEmpty()) {
            extractAllergyRisks(treatment, context);
            extractTreatmentRisks(treatment, context);
        }

        // 计算综合风险等级
        context.setRiskLevel(calculateOverallRiskLevel(context));

        log.info("医疗上下文构建完成，诊断={}, 风险标签数={}", diagnoses.size(), context.getRiskTags().size());
        return context;
    }

    private List<String> extractDiagnoses(MedicalRecord record) {
        List<String> diagnoses = new ArrayList<>();

        if (record.getDiagnosis() != null) {
            String diagnosis = record.getDiagnosis().trim();
            if (!diagnosis.isEmpty()) {
                String[] items = diagnosis.split("[,，、；;\\n]");
                for (String item : items) {
                    item = item.trim();
                    if (!item.isEmpty()) {
                        diagnoses.add(item);
                    }
                }
            }
        }

        if (diagnoses.isEmpty()) {
            diagnoses.add("未明确诊断");
        }

        return diagnoses;
    }

    private void extractDiseaseRisks(List<String> diagnoses, PatientContext context) {
        for (String diagnosis : diagnoses) {
            for (Map.Entry<String, DiseaseRiskInfo> entry : DISEASE_RISK_MAP.entrySet()) {
                if (diagnosis.contains(entry.getKey())) {
                    DiseaseRiskInfo riskInfo = entry.getValue();
                    RiskTag tag = new RiskTag();
                    tag.setType("疾病");
                    tag.setName(entry.getKey());
                    tag.setLevel(riskInfo.level);
                    tag.setAffectsDrugCategory(riskInfo.affectsDrugCategory);
                    tag.setRemark("诊断中提及: " + diagnosis);
                    context.addRiskTag(tag);
                    log.debug("提取到疾病风险标签: {}", tag.getName());
                    break;
                }
            }
        }
    }

    /** 从既往史中提取疾病风险 */
    private void extractDiseaseRisksFromPastHistory(String pastHistory, PatientContext context) {
        for (Map.Entry<String, DiseaseRiskInfo> entry : DISEASE_RISK_MAP.entrySet()) {
            if (pastHistory.contains(entry.getKey())) {
                // 避免重复添加
                boolean exists = context.getRiskTags().stream()
                        .anyMatch(t -> "疾病".equals(t.getType()) && t.getName().equals(entry.getKey()));
                if (exists) continue;
                DiseaseRiskInfo riskInfo = entry.getValue();
                RiskTag tag = new RiskTag();
                tag.setType("疾病");
                tag.setName(entry.getKey());
                tag.setLevel(riskInfo.level);
                tag.setAffectsDrugCategory(riskInfo.affectsDrugCategory);
                tag.setRemark("既往史中提及: " + pastHistory);
                context.addRiskTag(tag);
                log.debug("从既往史提取到疾病风险标签: {}", tag.getName());
            }
        }
    }

    private void extractAllergyRisks(String treatment, PatientContext context) {
        if (treatment == null || treatment.isEmpty()) return;

        for (Map.Entry<String, AllergyInfo> entry : ALLERGY_RISK_MAP.entrySet()) {
            if (treatment.contains(entry.getKey())) {
                AllergyInfo allergyInfo = entry.getValue();
                RiskTag tag = new RiskTag();
                tag.setType("过敏");
                tag.setName(entry.getKey());
                tag.setLevel(allergyInfo.level);
                tag.setAffectsDrugCategory(allergyInfo.affectsDrugCategory);
                tag.setRemark("处理意见中提及");
                context.addRiskTag(tag);
                log.warn("提取到过敏风险标签（危急）: {}", tag.getName());
            }
        }
    }

    private void extractTreatmentRisks(String treatment, PatientContext context) {
        if (treatment == null || treatment.isEmpty()) return;

        Pattern historyPattern = Pattern.compile("既往史[：:](.+?)(?:\n|现病史|过敏|$)");
        Matcher historyMatcher = historyPattern.matcher(treatment);
        if (historyMatcher.find()) {
            String historyText = historyMatcher.group(1).trim();
            String[] historyItems = historyText.split("[,，、；;]");
            for (String item : historyItems) {
                item = item.trim();
                if (!item.isEmpty()) {
                    extractDiseaseRisks(Collections.singletonList(item), context);
                }
            }
        }

        if (treatment.contains("肾功能不全") || treatment.contains("肌酐升高") || treatment.contains("尿素氮升高")) {
            RiskTag tag = new RiskTag();
            tag.setType("疾病");
            tag.setName("肾功能异常");
            tag.setLevel(RiskTag.RiskLevel.HIGH);
            tag.setAffectsDrugCategory("经肾代谢药物");
            tag.setRemark("处理意见中提及肾功能相关指标");
            context.addRiskTag(tag);
            log.debug("提取到肾功能风险标签");
        }

        if (treatment.contains("肝功能异常") || treatment.contains("转氨酶升高") || treatment.contains("ALT升高") || treatment.contains("AST升高")) {
            RiskTag tag = new RiskTag();
            tag.setType("疾病");
            tag.setName("肝功能异常");
            tag.setLevel(RiskTag.RiskLevel.HIGH);
            tag.setAffectsDrugCategory("经肝代谢药物");
            tag.setRemark("处理意见中提及肝功能相关指标");
            context.addRiskTag(tag);
            log.debug("提取到肝功能风险标签");
        }
    }

    private PatientContext.RiskLevel calculateOverallRiskLevel(PatientContext context) {
        if (context.getRiskTags().isEmpty()) return PatientContext.RiskLevel.LOW;

        boolean hasCritical = context.getRiskTags().stream()
                .anyMatch(t -> t.getLevel() == RiskTag.RiskLevel.CRITICAL);
        boolean hasHigh = context.getRiskTags().stream()
                .anyMatch(t -> t.getLevel() == RiskTag.RiskLevel.HIGH);

        if (hasCritical) return PatientContext.RiskLevel.CRITICAL;
        if (hasHigh) return PatientContext.RiskLevel.HIGH;
        return PatientContext.RiskLevel.MEDIUM;
    }

    private static class DiseaseRiskInfo {
        RiskTag.RiskLevel level;
        String affectsDrugCategory;
        DiseaseRiskInfo(RiskTag.RiskLevel level, String affectsDrugCategory) {
            this.level = level;
            this.affectsDrugCategory = affectsDrugCategory;
        }
    }

    private static class AllergyInfo {
        RiskTag.RiskLevel level;
        String affectsDrugCategory;
        AllergyInfo(RiskTag.RiskLevel level, String affectsDrugCategory) {
            this.level = level;
            this.affectsDrugCategory = affectsDrugCategory;
        }
    }
}
