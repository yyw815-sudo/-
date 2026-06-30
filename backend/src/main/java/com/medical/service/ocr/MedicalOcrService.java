package com.medical.service.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MedicalOcrService {
    private static final Logger logger = LoggerFactory.getLogger(MedicalOcrService.class);

    private static final double CONFIDENCE_THRESHOLD = 0.6;
    private static final double SCORE_HIGH = 0.85;
    private static final double SCORE_MEDIUM = 0.65;

    @Autowired
    private DeepSeekService deepSeekService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class RecognitionResult {
        private Map<String, String> fields;
        private Map<String, Double> scores;
        private Map<String, String> sources;
        private String rawText;
        private boolean needCorrection;

        public RecognitionResult() {
            this.fields = new HashMap<>();
            this.scores = new HashMap<>();
            this.sources = new HashMap<>();
            this.needCorrection = false;
        }

        public Map<String, String> getFields() { return fields; }
        public Map<String, Double> getScores() { return scores; }
        public Map<String, String> getSources() { return sources; }
        public String getRawText() { return rawText; }
        public boolean isNeedCorrection() { return needCorrection; }
        public void setRawText(String rawText) { this.rawText = rawText; }
        public void setNeedCorrection(boolean needCorrection) { this.needCorrection = needCorrection; }
    }

    public RecognitionResult recognize(byte[] imageBytes) {
        RecognitionResult result = new RecognitionResult();

        try {
            List<OCRWord> ocrWords = callBaiduOCR(imageBytes);

            // 修改八：confidence < 0.6 直接舍弃
            ocrWords = ocrWords.stream()
                    .filter(w -> w.getConfidence() >= CONFIDENCE_THRESHOLD)
                    .collect(Collectors.toList());

            if (ocrWords.isEmpty()) {
                logger.warn("OCR识别返回空或全部被置信度过滤");
                return result;
            }

            // 修改十五：OCR清洗（FSM之前）
            OCRNormalize.normalizeWords(ocrWords);
            logger.info("OCR清洗完成");

            String rawText = ocrWords.stream()
                    .map(OCRWord::getText)
                    .collect(Collectors.joining("\n"));
            result.setRawText(rawText);
            logger.info("OCR原文:\n{}", rawText);

            // ========== 第一层：FSM空间坐标匹配 ==========
            List<FieldResult> fsmResults = FSMEngine.match(ocrWords);
            logger.info("FSM匹配到 {} 个字段", fsmResults.size());

            // 收集LLM纠错的内容
            Map<String, String> llmInputFields = new HashMap<>();
            Map<String, FieldResult> bestFieldMap = new HashMap<>();

            for (FieldResult fr : fsmResults) {
                String fieldName = fr.getFieldName();
                String value = fr.getValue();
                double score = fr.getScore();

                // 修改九：Score阈值三级处理
                if (score >= SCORE_HIGH) {
                    // ≥0.85：直接通过
                    result.getFields().put(fieldName, value);
                    result.getScores().put(fieldName, score);
                    result.getSources().put(fieldName, "FSM");
                    bestFieldMap.put(fieldName, fr);
                } else if (score >= SCORE_MEDIUM) {
                    // 0.65~0.85：进入Regex验证
                    // 先尝试用Regex验证
                    String regexValue = RegexEngine.extractSingle(value, fieldName);
                    if (regexValue != null) {
                        result.getFields().put(fieldName, regexValue);
                        result.getScores().put(fieldName, score + 0.05);
                        result.getSources().put(fieldName, "REGEX");
                    } else {
                        llmInputFields.put(fieldName, value);
                    }
                    bestFieldMap.put(fieldName, fr);
                } else {
                    // <0.65：进入LLM纠错
                    llmInputFields.put(fieldName, value);
                    bestFieldMap.put(fieldName, fr);
                }
            }

            // ========== 第二层：Regex规则引擎 ==========
            List<RegexEngine.RegexMatch> regexMatches = RegexEngine.extract(ocrWords);
            for (RegexEngine.RegexMatch match : regexMatches) {
                String fieldName = match.getFieldName();
                String value = match.getValue();
                double score = match.getConfidence();

                FieldStrategy strategy = FieldStrategy.findByFieldKey(fieldName);
                String displayName = (strategy != null) ? strategy.getDisplayName() : fieldName;

                if (!result.getFields().containsKey(displayName) ||
                        score > result.getScores().getOrDefault(displayName, 0.0)) {
                    result.getFields().put(displayName, value);
                    result.getScores().put(displayName, score);
                    result.getSources().put(displayName, "REGEX");
                }
            }

            // ========== 第三层：Validator校验 ==========
            Map<String, String> fieldsToRemove = new HashMap<>();
            for (Map.Entry<String, String> entry : result.getFields().entrySet()) {
                Validator.ValidationResult validation = Validator.validate(entry.getKey(), entry.getValue());
                if (!validation.isValid()) {
                    logger.warn("字段[{}]校验失败: {} - {}", entry.getKey(), entry.getValue(), validation.getErrorMessage());
                    fieldsToRemove.put(entry.getKey(), entry.getValue());
                    // 添加到LLM纠错
                    llmInputFields.put(entry.getKey(), entry.getValue());
                }
            }
            fieldsToRemove.keySet().forEach(result.getFields()::remove);
            fieldsToRemove.keySet().forEach(result.getScores()::remove);

            // ========== 第四层：LLM语义纠错 ==========
            // 触发条件：有低分字段、全空、或缺失核心字段（诊断/主诉/医生/医院/处方）
            Set<String> CORE_FIELDS = new java.util.HashSet<>(Arrays.asList(
                    "诊断", "主诉", "医生", "医院", "处方", "姓名", "现病史", "既往史", "药品"));
            boolean missingCoreFields = result.getFields().keySet().stream()
                    .noneMatch(k -> CORE_FIELDS.contains(k));
            boolean allFieldsEmpty = result.getFields().isEmpty();
            if (!llmInputFields.isEmpty() || allFieldsEmpty || missingCoreFields) {
                result.setNeedCorrection(true);
                logger.info("触发LLM纠错，待纠错字段数: {}，缺失核心字段: {}",
                        llmInputFields.size(), missingCoreFields);

                try {
                    // 如果没有待纠错字段但全是空的，也调用LLM尝试全面提取
                    Map<String, String> llmInput = llmInputFields.isEmpty() ? result.getFields() : llmInputFields;
                    Map<String, String> correctedFields = deepSeekService.correctFields(rawText, llmInput);
                    for (Map.Entry<String, String> entry : correctedFields.entrySet()) {
                        String fieldName = entry.getKey();
                        String value = entry.getValue();

                        String displayName = fieldName;
                        FieldStrategy strategy = FieldStrategy.findByFieldKey(fieldName);
                        if (strategy != null) {
                            displayName = strategy.getDisplayName();
                            // 跳过allowLlm=false的字段（如电话、身份证）
                            if (!strategy.isAllowLlm()) continue;
                        }

                        if (value != null && !value.isEmpty()) {
                            result.getFields().put(displayName, value);
                            result.getScores().put(displayName, 0.85);
                            result.getSources().put(displayName, "LLM");
                        }
                    }
                } catch (Exception e) {
                    logger.error("LLM纠错失败: {}", e.getMessage());
                    for (Map.Entry<String, String> entry : llmInputFields.entrySet()) {
                        if (!result.getFields().containsKey(entry.getKey())) {
                            result.getFields().put(entry.getKey(), entry.getValue());
                            result.getSources().put(entry.getKey(), "FALLBACK");
                        }
                    }
                }
            }

            // ========== 第五层：本地格式整理（替代DS调用，更快更可靠） ==========
            if (!result.getFields().isEmpty()) {
                try {
                    Map<String, String> cleaned = cleanFieldValues(result.getFields());
                    for (Map.Entry<String, String> entry : cleaned.entrySet()) {
                        if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                            result.getFields().put(entry.getKey(), entry.getValue());
                        }
                    }
                    logger.info("本地格式整理完成");
                } catch (Exception e) {
                    logger.error("格式整理异常(不阻塞): {}", e.getMessage());
                }
            }

            logger.info("识别完成，提取字段数: {}", result.getFields().size());
            logger.info("字段详情: {}", result.getFields());
            logger.info("字段得分: {}", result.getScores());
            logger.info("字段来源: {}", result.getSources());

        } catch (Exception e) {
            logger.error("识别流程异常: {}", e.getMessage(), e);
        }

        return result;
    }

    private List<OCRWord> callBaiduOCR(byte[] imageBytes) throws Exception {
        List<OCRWord> words = new ArrayList<>();

        String accessToken = getAccessToken();
        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate?access_token=" + accessToken;

        String imgBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
        String body = "image=" + java.net.URLEncoder.encode(imgBase64, "UTF-8");

        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }

        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        JsonNode root = objectMapper.readTree(response.toString());
        JsonNode wordsResult = root.get("words_result");

        if (wordsResult != null && wordsResult.isArray()) {
            for (JsonNode item : wordsResult) {
                String text = item.has("words") ? item.get("words").asText() : "";
                JsonNode location = item.get("location");

                if (location != null) {
                    int left = location.has("left") ? location.get("left").asInt() : 0;
                    int top = location.has("top") ? location.get("top").asInt() : 0;
                    int width = location.has("width") ? location.get("width").asInt() : 0;
                    int height = location.has("height") ? location.get("height").asInt() : 0;
                    double confidence = item.has("confidence") ? item.get("confidence").asDouble() : 1.0;

                    if (!text.isEmpty()) {
                        words.add(new OCRWord(text, left, top, width, height, confidence));
                    }
                }
            }
        }

        words.sort(Comparator.comparingInt(OCRWord::getTop).thenComparingInt(OCRWord::getLeft));
        return words;
    }

    /**
     * 本地规则格式整理：替代DS调用，快速清洗字段值
     */
    private Map<String, String> cleanFieldValues(Map<String, String> fields) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            if (val == null || val.isEmpty()) continue;

            if (key.equals("性别")) {
                val = val.replaceAll("[^男女]", ""); // 只保留"男"或"女"
            } else if (key.equals("年龄")) {
                val = val.replaceAll("[^0-9]", ""); // 只保留数字
            } else if (key.equals("日期") || key.equals("就诊日期") || key.equals("visitDate")) {
                // 统一为YYYY-MM-DD
                val = val.replaceAll("[年月]", "-").replaceAll("日", "").trim();
            } else {
                // 通用清洗
                val = val.replaceAll("[\\s　]+", "").trim(); // 清理多余空格
            }
            result.put(key, val);
        }
        return result;
    }

    private String getAccessToken() throws Exception {
        String apiKey = "PJecCd0K7luLtDgMY9pQx7RL";
        String secretKey = "qPMOezTT19KdHKQNvQEYVuij8a3bCLb4";

        String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey;
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        conn.setRequestMethod("GET");

        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        JsonNode root = objectMapper.readTree(response.toString());
        return root.get("access_token").asText();
    }
}
