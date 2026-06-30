package com.medical.service.ai;

import com.medical.entity.MedicalRecord;
import com.medical.entity.MedicationPlan;
import com.medical.service.ai.context.MedicalContextBuilder;
import com.medical.service.ai.context.PatientContext;
import com.medical.service.ocr.DeepSeekService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MedicationAIService {

    private final MedicalContextBuilder contextBuilder;
    private final MedicalParser medicalParser;
    private final RuleEngine ruleEngine;
    private final RecommendationEngine recommendationEngine;
    private final PromptBuilder promptBuilder;
    private final DeepSeekService deepSeekService;
    private final PlanValidator planValidator;
    private final MedicationPlanGenerator planGenerator;

    public MedicationAIService(MedicalContextBuilder contextBuilder, MedicalParser medicalParser, RuleEngine ruleEngine, RecommendationEngine recommendationEngine, PromptBuilder promptBuilder, DeepSeekService deepSeekService, PlanValidator planValidator, MedicationPlanGenerator planGenerator) {
        this.contextBuilder = contextBuilder;
        this.medicalParser = medicalParser;
        this.ruleEngine = ruleEngine;
        this.recommendationEngine = recommendationEngine;
        this.promptBuilder = promptBuilder;
        this.deepSeekService = deepSeekService;
        this.planValidator = planValidator;
        this.planGenerator = planGenerator;
    }

    @Data
    public static class AnalysisResult {
        private boolean success;
        private List<MedicationPlan> plans;
        private List<String> warnings;
        private List<String> errors;
        private PatientContext context;
        private TreatmentContext treatment;
        private RuleResult ruleResult;
        private List<Recommendation> recommendations;
        private Integer version;
    }

    public AnalysisResult analyzeAndGenerate(Long userId, MedicalRecord record) {
        log.info("========== 开始AI用药分析流程（八层流水线） ==========");
        long startTime = System.currentTimeMillis();
        long stepStartTime;
        AnalysisResult result = new AnalysisResult();
        result.setSuccess(false);
        result.setErrors(new ArrayList<>());
        result.setWarnings(new ArrayList<>());

        try {
            stepStartTime = System.currentTimeMillis();
            log.info("步骤1: 医疗上下文构建");
            PatientContext context = contextBuilder.buildContext(record);
            result.setContext(context);
            log.info("步骤1完成，耗时: {}ms", System.currentTimeMillis() - stepStartTime);

            if (context.getRiskLevel() == PatientContext.RiskLevel.CRITICAL) {
                result.getWarnings().add("检测到危急级别风险，需重点关注");
                log.warn("检测到危急级别风险");
            }

            stepStartTime = System.currentTimeMillis();
            log.info("步骤2: 病历解析");
            TreatmentContext treatment = medicalParser.parse(context);
            result.setTreatment(treatment);
            log.info("步骤2完成，耗时: {}ms", System.currentTimeMillis() - stepStartTime);

            if (treatment.getMedicines() == null || treatment.getMedicines().isEmpty()) {
                result.getErrors().add("未能从病历中提取药品信息");
                log.warn("病历解析未提取到药品信息");
                return result;
            }

            stepStartTime = System.currentTimeMillis();
            log.info("步骤3: 规则引擎检测");
            RuleResult ruleResult = ruleEngine.execute(context, treatment);
            result.setRuleResult(ruleResult);
            log.info("步骤3完成，耗时: {}ms", System.currentTimeMillis() - stepStartTime);

            if (ruleResult.isHasConflict()) {
                log.warn("检测到 {} 个药物冲突", ruleResult.getConflicts().size());
                for (RuleEngine.ConflictInfo c : ruleResult.getConflicts()) {
                    String conflictMsg = c.getDrugA() + " + " + c.getDrugB() + " = " + c.getLevel() + "冲突";
                    if (c.getDescription() != null && !c.getDescription().isEmpty()) conflictMsg += "（" + c.getDescription() + "）";
                    result.getWarnings().add(conflictMsg);
                }
            }

            if (!ruleResult.getRiskTags().isEmpty()) {
                log.warn("检测到 {} 个风险标签", ruleResult.getRiskTags().size());
                for (String tag : ruleResult.getRiskTags()) result.getWarnings().add(tag);
            }

            stepStartTime = System.currentTimeMillis();
            log.info("步骤4: 建议生成");
            List<Recommendation> recommendations = recommendationEngine.generateRecommendations(context, treatment, ruleResult);
            result.setRecommendations(recommendations);
            log.info("步骤4完成，耗时: {}ms", System.currentTimeMillis() - stepStartTime);

            stepStartTime = System.currentTimeMillis();
            log.info("步骤5: 构建Prompt");
            String prompt = promptBuilder.buildPrompt(context, treatment, ruleResult, recommendations);
            log.info("步骤5完成，耗时: {}ms", System.currentTimeMillis() - stepStartTime);

            stepStartTime = System.currentTimeMillis();
            log.info("步骤6: 调用LLM推理");
            String aiResponse = deepSeekService.callTextModel(prompt);
            log.info("AI返回内容: {}, 耗时: {}ms", aiResponse.length() > 1000 ? "长度=" + aiResponse.length() : aiResponse, System.currentTimeMillis() - stepStartTime);

            String businessContent = extractBusinessContent(aiResponse);
            log.info("提取的业务内容: {}", businessContent.length() > 500 ? businessContent.substring(0, 500) + "..." : businessContent);

            stepStartTime = System.currentTimeMillis();
            log.info("步骤7: 验证AI返回");
            PlanValidator.ValidationResult validation = planValidator.validate(businessContent, context);
            log.info("步骤7完成，耗时: {}ms", System.currentTimeMillis() - stepStartTime);

            if (!validation.isValid()) {
                result.getErrors().addAll(validation.getErrors());
                log.error("AI返回验证失败: {}", validation.getErrors());
                return result;
            }

            if (validation.getWarnings() != null && !validation.getWarnings().isEmpty()) result.getWarnings().addAll(validation.getWarnings());

            stepStartTime = System.currentTimeMillis();
            log.info("步骤8: 生成用药计划（含替代药物方案）");
            MedicationPlanGenerator.GeneratedResult generated = planGenerator.generate(userId, record.getRecordId(), validation.getPlanJson(), validation.getAlternatives());
            result.setPlans(generated.getPlans());

            if (generated.getAlternativeInfo() != null && !generated.getAlternativeInfo().isEmpty())
                for (String altInfo : generated.getAlternativeInfo()) result.getWarnings().add("替代方案: " + altInfo);

            log.info("步骤8完成，耗时: {}ms", System.currentTimeMillis() - stepStartTime);

            if (generated.getIsExisting()) log.info("该病历已有进行中的用药计划，跳过重复分析");
            if (generated.getWarnings() != null) result.getWarnings().addAll(generated.getWarnings());

            result.setSuccess(true);
            log.info("========== AI用药分析流程完成 ==========");
            log.info("总耗时: {}ms，风险评分: {}/100, 风险等级: {}, 生成计划数: {}", System.currentTimeMillis() - startTime, ruleResult.getRiskScore(), ruleResult.getRiskLevel(), generated.getPlans().size());

        } catch (Exception e) {
            log.error("AI用药分析流程异常", e);
            result.getErrors().add("分析过程发生异常: " + e.getMessage());
        }
        return result;
    }

    private String extractBusinessContent(String apiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(apiResponse);
            JsonNode choices = root.get("choices");
            if (choices != null && !choices.isEmpty()) {
                JsonNode message = choices.get(0).get("message");
                if (message != null) {
                    String content = message.get("content").asText();
                    content = content.trim();
                    if (content.startsWith("```json")) content = content.substring(7);
                    if (content.endsWith("```")) content = content.substring(0, content.length() - 3);
                    return content.trim();
                }
            }
        } catch (Exception e) { log.warn("解析API响应失败，直接返回原始内容", e); }
        return apiResponse;
    }
}
