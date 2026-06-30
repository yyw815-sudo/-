package com.medical.service.ai;

import com.medical.service.ai.context.PatientContext;
import com.medical.service.ai.context.RiskTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class PromptBuilder {

    private static final String JSON_SCHEMA = """
        {
            "$schema": "http://json-schema.org/draft-07/schema#",
            "type": "object",
            "required": ["plan", "warnings"],
            "properties": {
                "plan": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "required": ["time", "medicine", "dose", "remark"],
                        "properties": {
                            "time": {"type": "string", "pattern": "^([01]\\\\d|2[0-3]):[0-5]\\\\d$", "description": "服药时间，格式HH:mm"},
                            "medicine": {"type": "string", "minLength": 1, "maxLength": 100, "description": "药品名称"},
                            "dose": {"type": "string", "minLength": 1, "maxLength": 50, "description": "用药剂量"},
                            "remark": {"type": "string", "maxLength": 200, "description": "备注，如饭前/饭后"}
                        }
                    }
                },
                "warnings": {
                    "type": "array",
                    "items": {"type": "string", "maxLength": 500}
                },
                "alternatives": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "required": ["originalDrug", "alternativeDrug", "reason"],
                        "properties": {
                            "originalDrug": {"type": "string", "description": "原药物名称（存在冲突的药物）"},
                            "alternativeDrug": {"type": "string", "description": "替代药物名称"},
                            "dose": {"type": "string", "description": "替代药物剂量"},
                            "frequency": {"type": "string", "description": "替代药物服用频率"},
                            "route": {"type": "string", "description": "替代药物服用途径"},
                            "reason": {"type": "string", "description": "替换原因"}
                        }
                    }
                }
            }
        }
        """;

    public String buildPrompt(PatientContext context, TreatmentContext treatment, RuleResult ruleResult, List<Recommendation> recommendations) {
        log.info("开始构建AI Prompt（JSON Schema版本）");
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名临床药师。\n");
        prompt.append("请根据以下结构化医疗信息生成慢性病用药计划。\n\n");

        prompt.append("【患者信息】\n");
        if (context.getAge() != null) prompt.append("年龄：").append(context.getAge()).append("\n");
        if (context.getGender() != null && !context.getGender().isEmpty()) prompt.append("性别：").append(context.getGender()).append("\n");

        prompt.append("【疾病诊断】\n");
        if (context.getDiagnoses() != null && !context.getDiagnoses().isEmpty()) prompt.append(String.join("、", context.getDiagnoses())).append("\n");
        else prompt.append("未提供\n");

        prompt.append("\n【现病史】\n");
        if (context.getPresentHistory() != null && !context.getPresentHistory().isEmpty()) prompt.append(context.getPresentHistory()).append("\n");
        else prompt.append("未提供\n");

        prompt.append("\n【既往史】\n");
        if (context.getPastHistory() != null && !context.getPastHistory().isEmpty()) prompt.append(context.getPastHistory()).append("\n");
        else prompt.append("未提供\n");

        prompt.append("\n【患者风险摘要】\n");
        if (context.getRiskTags() != null && !context.getRiskTags().isEmpty()) {
            for (RiskTag tag : context.getRiskTags()) {
                prompt.append("- ").append(tag.getType()).append(": ").append(tag.getName());
                prompt.append(" (").append(tag.getLevel()).append(")");
                if (tag.getAffectsDrugCategory() != null && !tag.getAffectsDrugCategory().isEmpty())
                    prompt.append(" → 影响: ").append(tag.getAffectsDrugCategory());
                prompt.append("\n");
            }
        } else prompt.append("无特殊风险\n");

        if (context.getAllergyTags() != null && !context.getAllergyTags().isEmpty()) {
            prompt.append("\n【过敏风险（需重点注意）】\n");
            for (RiskTag allergy : context.getAllergyTags()) {
                prompt.append("- ").append(allergy.getName());
                if (allergy.getAffectsDrugCategory() != null) prompt.append(" → 禁用: ").append(allergy.getAffectsDrugCategory());
                prompt.append("\n");
            }
        }

        prompt.append("\n【当前药物】\n");
        if (treatment.getMedicines() != null && !treatment.getMedicines().isEmpty()) {
            int index = 1;
            for (TreatmentContext.MedicineItem medicine : treatment.getMedicines()) {
                prompt.append(index++).append(". ").append(medicine.getName());
                if (medicine.getDose() != null) prompt.append(" ").append(medicine.getDose());
                if (medicine.getFrequency() != null) prompt.append("，").append(medicine.getFrequency());
                if (medicine.getRoute() != null) prompt.append("，").append(medicine.getRoute());
                prompt.append("\n");
            }
        } else prompt.append("未提供\n");

        prompt.append("\n【风险评估】\n");
        prompt.append("风险评分：").append(ruleResult.getRiskScore()).append("/100\n");
        prompt.append("风险等级：").append(ruleResult.getRiskLevel()).append("\n");

        prompt.append("\n【药物冲突】\n");
        if (ruleResult.isHasConflict() && ruleResult.getConflicts() != null && !ruleResult.getConflicts().isEmpty()) {
            for (RuleEngine.ConflictInfo conflict : ruleResult.getConflicts()) {
                prompt.append(conflict.getDrugA()).append(" + ").append(conflict.getDrugB());
                prompt.append(" = ").append(conflict.getLevel()).append("冲突");
                if (conflict.getDescription() != null && !conflict.getDescription().isEmpty()) prompt.append("（").append(conflict.getDescription()).append("）");
                if (conflict.getSuggestion() != null && !conflict.getSuggestion().isEmpty()) prompt.append(" 建议: ").append(conflict.getSuggestion());
                prompt.append("\n");
            }
        } else prompt.append("无\n");

        if (recommendations != null && !recommendations.isEmpty()) {
            prompt.append("\n【规则建议】\n");
            for (Recommendation rec : recommendations) {
                prompt.append("[").append(rec.getPriority()).append("] ").append(rec.getTitle()).append(": ").append(rec.getContent()).append("\n");
            }
        }

        if (treatment.getAdvices() != null && !treatment.getAdvices().isEmpty()) {
            prompt.append("\n【医嘱建议】\n");
            for (String advice : treatment.getAdvices()) prompt.append("- ").append(advice).append("\n");
        }

        prompt.append("\n【要求】\n");
        prompt.append("1. 根据患者风险摘要调整用药方案\n");
        prompt.append("2. 严格避免患者过敏的药物\n");
        prompt.append("3. 安排每日服药时间，避免药物冲突\n");
        prompt.append("4. 标注饭前/饭后或其他注意事项\n");
        prompt.append("5. 给出用药注意事项，特别是针对患者风险的注意事项\n");
        prompt.append("6. 【重要】当存在药物冲突时，必须提供替代药物方案，不能只是警告！\n");
        prompt.append("7. 替代药物应该：与原药物疗效相似但无冲突、剂量合理、符合患者病史\n");
        prompt.append("8. 参考【现病史】和【既往史】信息评估患者整体状况，调整用药方案\n");
        prompt.append("9. 如果现病史或既往史中有影响用药的信息（如肝肾功能问题、其他并发症），请在warnings中给出提示\n");
        prompt.append("10. 替代方案需写入alternatives数组，并在plan中体现替代药物的计划\n");
        prompt.append("11. 必须严格遵守以下JSON Schema格式输出\n");
        prompt.append("12. 不要输出任何Markdown格式或额外文字，只输出JSON\n");

        prompt.append("\n【JSON Schema】\n").append(JSON_SCHEMA);

        prompt.append("\n【输出示例】\n");
        prompt.append("{\n");
        prompt.append("  \"plan\": [\n");
        prompt.append("    {\"time\": \"08:00\", \"medicine\": \"药品名\", \"dose\": \"80mg\", \"remark\": \"早餐后\"},\n");
        prompt.append("    {\"time\": \"18:00\", \"medicine\": \"药品名\", \"dose\": \"500mg\", \"remark\": \"晚餐后\"}\n");
        prompt.append("  ],\n");
        prompt.append("  \"warnings\": [\"注意事项1\", \"注意事项2\"],\n");
        prompt.append("  \"alternatives\": [\n");
        prompt.append("    {\"originalDrug\": \"冲突药物A\", \"alternativeDrug\": \"替代药物X\", \"dose\": \"50mg\", \"frequency\": \"每日1次\", \"route\": \"口服\", \"reason\": \"药物A与药物B存在严重相互作用\"}\n");
        prompt.append("  ]\n");
        prompt.append("}\n");

        log.info("Prompt构建完成（JSON Schema版本），长度={}", prompt.length());
        return prompt.toString();
    }
}
