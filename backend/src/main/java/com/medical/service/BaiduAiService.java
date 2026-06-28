package com.medical.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class BaiduAiService {

    private final BaiduTokenService tokenService;

    public BaiduAiService(BaiduTokenService tokenService) {
        this.tokenService = tokenService;
    }

    /** 调用文心一言分析处方，返回结构化JSON */
    public String analyzePrescription(String prescriptionText) {
        try {
            String accessToken = tokenService.getAccessToken();
            String url = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant"
                    + "?access_token=" + accessToken;

            String prompt = "你是一个专业的药学分析系统。请分析以下处方内容，提取用药信息，"
                    + "以严格的JSON格式返回，不要包含任何其他文字。\n\n处方内容：" + prescriptionText
                    + "\n\n请按以下JSON格式返回：\n"
                    + "{\"medications\": [{\"medicineName\": \"药品名\", \"dosage\": \"每次用量\", "
                    + "\"frequency\": \"用药频率\", \"timesPerDay\": 每日次数, \"intervalHours\": 间隔小时数, "
                    + "\"startDate\": \"YYYY-MM-DD\", \"endDate\": \"YYYY-MM-DD\", \"purpose\": \"用药目的\"}]}";

            String body = "{\"messages\":[{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}],"
                    + "\"temperature\":0.1,\"top_p\":1}";

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line);
            }

            // 解析文心一言返回
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            
            if (!root.has("result")) {
                throw new RuntimeException("API响应格式异常: " + response.toString());
            }
            String resultContent = root.get("result").asText();

            // 清理可能的markdown标记
            resultContent = resultContent.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();

            return resultContent;
        } catch (Exception e) {
            throw new RuntimeException("AI分析处方失败: " + e.getMessage(), e);
        }
    }

    /** 冲突校正 */
    public String correctPlanWithConflict(String originalText, String conflictInfo) {
        try {
            String accessToken = tokenService.getAccessToken();
            String url = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant"
                    + "?access_token=" + accessToken;

            String prompt = "用户原有的处方为：" + originalText
                    + "\n\n检测到以下药品冲突信息：" + conflictInfo
                    + "\n\n请根据冲突信息调整用药方案，以严格的JSON格式返回调整后的用药计划，不要包含任何其他文字。"
                    + "格式：{\"medications\": [{\"medicineName\": \"药品名\", \"dosage\": \"每次用量\", "
                    + "\"frequency\": \"用药频率\", \"timesPerDay\": 每日次数, \"intervalHours\": 间隔小时数, "
                    + "\"startDate\": \"YYYY-MM-DD\", \"endDate\": \"YYYY-MM-DD\", \"purpose\": \"用药目的\"}]}";

            String body = "{\"messages\":[{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}],"
                    + "\"temperature\":0.1,\"top_p\":1}";

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            
            if (!root.has("result")) {
                throw new RuntimeException("API响应格式异常: " + response.toString());
            }
            String resultContent = root.get("result").asText();
            resultContent = resultContent.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            return resultContent;
        } catch (Exception e) {
            throw new RuntimeException("AI冲突校正失败: " + e.getMessage(), e);
        }
    }

    /** 通用AI问答接口 */
    public String ask(String prompt) {
        try {
            String accessToken = tokenService.getAccessToken();
            String url = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant"
                    + "?access_token=" + accessToken;

            String body = "{\"messages\":[{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}],"
                    + "\"temperature\":0.1,\"top_p\":1}";

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            if (!root.has("result")) {
                throw new RuntimeException("API响应格式异常: " + response.toString());
            }
            return root.get("result").asText();
        } catch (Exception e) {
            throw new RuntimeException("AI请求失败: " + e.getMessage(), e);
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
