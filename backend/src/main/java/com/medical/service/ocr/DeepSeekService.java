package com.medical.service.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DeepSeekService {
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekService.class);

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> correctFields(String ocrText, Map<String, String> currentFields) {
        String prompt = buildPrompt(ocrText, currentFields);

        try {
            String response = callDeepSeek(prompt);
            return parseResponse(response);
        } catch (Exception e) {
            logger.error("DeepSeek调用失败: {}", e.getMessage());
            return currentFields;
        }
    }

    /**
     * 最终格式整理：在填充前端表单前，让DS统一清洗/规范化所有字段
     */
    public Map<String, String> formatAndClean(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是医疗病历格式整理专家。请将以下OCR提取的医疗字段进行格式化清洗，请严格遵循以下规则：\n\n");
        sb.append("1. 性别：统一为 [男] 或 [女]，不保留其他表述\n");
        sb.append("2. 年龄：只保留数字，去掉 [岁] 字\n");
        sb.append("3. 日期：统一为YYYY-MM-DD格式\n");
        sb.append("4. 诊断/主诉/现病史/既往史/处理意见：清除多余空格、换行符、特殊符号，保持中文通顺\n");
        sb.append("5. 如果字段值为空或无效则返回空字符串\n\n");

        sb.append("待整理的字段：\n");
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }

        sb.append("\n请严格按以下JSON格式输出（只输出JSON，不要包含其他文字）：\n");
        sb.append("{\n");
        sb.append("  \"性别\": \"值\",\n");
        sb.append("  \"年龄\": \"值\",\n");
        sb.append("  \"日期\": \"值\",\n");
        sb.append("  \"主诉\": \"值\",\n");
        sb.append("  \"现病史\": \"值\",\n");
        sb.append("  \"既往史\": \"值\",\n");
        sb.append("  \"诊断\": \"值\",\n");
        sb.append("  \"处理意见\": \"值\"\n");
        sb.append("}");

        try {
            String response = callDeepSeek(sb.toString());
            Map<String, String> result = new LinkedHashMap<>();
            String content = extractJsonContent(response);
            if (content == null) return fields;

            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(content);
            String[] expectedKeys = {"性别", "年龄", "日期", "主诉", "现病史", "既往史", "诊断", "处理意见"};
            for (String key : expectedKeys) {
                com.fasterxml.jackson.databind.JsonNode node = root.get(key);
                if (node != null && !node.isNull()) {
                    String val = node.asText().trim();
                    result.put(key, val);
                }
            }
            logger.info("DS格式整理完成: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("DS格式整理失败: {}", e.getMessage());
            return fields;
        }
    }

    private String extractJsonContent(String rawResponse) {
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(rawResponse);
            com.fasterxml.jackson.databind.JsonNode choices = root.get("choices");
            if (choices != null && !choices.isEmpty()) {
                String content = choices.get(0).get("message").get("content").asText();
                content = content.trim();
                if (content.startsWith("```json")) content = content.substring(7);
                if (content.endsWith("```")) content = content.substring(0, content.length() - 3);
                return content.trim();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String buildPrompt(String ocrText, Map<String, String> currentFields) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是医疗单据解析专家。请分析以下OCR识别结果，提取医疗信息并按JSON格式输出。\n\n");
        sb.append("识别文本：\n");
        sb.append(ocrText);
        sb.append("\n\n");

        if (!currentFields.isEmpty()) {
            sb.append("已识别的字段（可能有误，请修正）：\n");
            for (Map.Entry<String, String> entry : currentFields.entrySet()) {
                sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("请提取以下字段（如果识别不到请返回空字符串）。重要：请严格区分字段含义，不要将主诉内容写入诊断字段：\n");
        sb.append("- name: 患者姓名\n");
        sb.append("- gender: 性别（男/女）\n");
        sb.append("- age: 年龄\n");
        sb.append("- phone: 手机号\n");
        sb.append("- idCard: 身份证号\n");
        sb.append("- visitDate: 就诊日期（YYYY-MM-DD格式）。注意：文档中可能包含多个日期，请返回就诊/挂号日期，不要返回其他日期\n");
        sb.append("- department: 科室\n");
        sb.append("- chiefComplaint: 主诉，即患者自述的症状（如[头晕10天]），不要写入诊断字段\n");
        sb.append("- presentHistory: 现病史\n");
        sb.append("- pastHistory: 既往史\n");
        sb.append("- physicalExam: 体格检查\n");
        sb.append("- auxiliaryExam: 辅助检查\n");
        sb.append("- diagnosis: 诊断，即医生给出的诊断结论（如[高血压]），不要包含主诉内容\n");
        sb.append("- treatment: 处理意见，请包含处方药品信息（药品名称、用法用量等）\n");
        sb.append("- doctor: 医生姓名\n");

        sb.append("\n输出格式要求：纯JSON格式，不要包含其他内容。");

        return sb.toString();
    }

    private String callDeepSeek(String prompt) throws Exception {
        URL url = new URL(baseUrl + "/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(30000);
        conn.setDoOutput(true);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("temperature", 0.1);

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        java.util.List<Map<String, String>> messages = new java.util.ArrayList<>();
        messages.add(message);
        requestBody.put("messages", messages);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(objectMapper.writeValueAsString(requestBody).getBytes("UTF-8"));
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        conn.disconnect();

        return response.toString();
    }

    private Map<String, String> parseResponse(String response) {
        Map<String, String> result = new HashMap<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");

            if (choices != null && !choices.isEmpty()) {
                JsonNode message = choices.get(0).get("message");
                if (message != null) {
                    String content = message.get("content").asText();

                    content = content.trim();
                    if (content.startsWith("```json")) {
                        content = content.substring(7);
                    }
                    if (content.endsWith("```")) {
                        content = content.substring(0, content.length() - 3);
                    }
                    content = content.trim();

                    JsonNode jsonContent = objectMapper.readTree(content);

                    String[] fields = {"name", "gender", "age", "phone", "idCard", "visitDate",
                            "department", "chiefComplaint", "presentHistory", "pastHistory",
                            "physicalExam", "auxiliaryExam", "diagnosis", "treatment", "doctor", "medicines"};

                    for (String field : fields) {
                        JsonNode valueNode = jsonContent.get(field);
                        if (valueNode != null && !valueNode.isNull()) {
                            String value = valueNode.asText().trim();
                            if (!value.isEmpty()) {
                                result.put(field, value);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("DeepSeek响应解析失败: {}", e.getMessage());
        }

        return result;
    }
}
