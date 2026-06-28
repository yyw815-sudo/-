package com.medical.service;

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
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BaiduOcrService {

    private static final Logger log = LoggerFactory.getLogger(BaiduOcrService.class);

    @Value("${baidu.ai.api-key:}")
    private String apiKey;

    @Value("${baidu.ai.secret-key:}")
    private String secretKey;

    /** 调用百度OCR识别图片中的文字，返回合并后的纯文本 */
    public String recognizeText(byte[] imageBytes) {
        try {
            String accessToken = getAccessToken();
            String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic"
                    + "?access_token=" + accessToken;

            String imgBase64 = Base64.getEncoder().encodeToString(imageBytes);
            String body = "image=" + URLEncoder.encode(imgBase64, "UTF-8");

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
            JsonNode wordsResult = root.get("words_result");

            if (wordsResult != null && wordsResult.isArray()) {
                StringBuilder textBuilder = new StringBuilder();
                for (JsonNode item : wordsResult) {
                    String words = item.get("words").asText();
                    if (textBuilder.length() > 0) textBuilder.append("\n");
                    textBuilder.append(words);
                }
                return textBuilder.toString();
            }

            return "";
        } catch (Exception e) {
            throw new RuntimeException("OCR识别失败: " + e.getMessage(), e);
        }
    }

    /** 调用百度医疗检验报告单识别，返回结构化JSON数据 */
    public Map<String, String> recognizeMedicalReport(byte[] imageBytes) {
        Map<String, String> result = new HashMap<>();
        
        try {
            String accessToken = getAccessToken();
            String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/medical_report_detection"
                    + "?access_token=" + accessToken;

            String imgBase64 = Base64.getEncoder().encodeToString(imageBytes);
            String body = "image=" + URLEncoder.encode(imgBase64, "UTF-8");

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
            
            if (root.has("error_code")) {
                throw new RuntimeException("医疗报告识别失败: " + root.get("error_msg").asText());
            }

            JsonNode wordsResult = root.get("words_result");
            if (wordsResult != null) {
                if (wordsResult.has("CommonData")) {
                    JsonNode commonData = wordsResult.get("CommonData");
                    for (JsonNode item : commonData) {
                        String key = item.has("word_name") ? item.get("word_name").asText() : "";
                        String value = item.has("word") ? item.get("word").asText() : "";
                        result.put(key, value);
                    }
                }
                if (wordsResult.has("Item")) {
                    StringBuilder items = new StringBuilder();
                    JsonNode itemsNode = wordsResult.get("Item");
                    for (JsonNode item : itemsNode) {
                        if (items.length() > 0) items.append("\n");
                        items.append(item.toString());
                    }
                    result.put("items", items.toString());
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("医疗报告识别失败: " + e.getMessage(), e);
        }
    }

    /** 调用百度诊断报告识别(health_report)，返回结构化JSON数据 */
    public Map<String, String> recognizeHealthReport(byte[] imageBytes) {
        Map<String, String> result = new HashMap<>();
        
        try {
            String accessToken = getAccessToken();
            String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/health_report"
                    + "?access_token=" + accessToken;

            String imgBase64 = Base64.getEncoder().encodeToString(imageBytes);
            String body = "image=" + URLEncoder.encode(imgBase64, "UTF-8");

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
            
            // 打印原始返回结果
            log.info("【百度诊断报告识别】原始返回: {}", root.toString());
            
            if (root.has("error_code")) {
                throw new RuntimeException("诊断报告识别失败: " + root.get("error_msg").asText());
            }

            JsonNode wordsResult = root.get("words_result");
            if (wordsResult != null && wordsResult.isArray()) {
                for (JsonNode item : wordsResult) {
                    String key = item.has("word_name") ? decodeUnicode(item.get("word_name").asText()) : "";
                    String value = item.has("word") ? decodeUnicode(item.get("word").asText()) : "";
                    result.put(key, value);
                }
            }
            
            // 打印解析后的字段映射
            log.info("【百度诊断报告识别】解析后字段: {}", result);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("诊断报告识别失败: " + e.getMessage(), e);
        }
    }
    
    /** 解码 Unicode 转义字符（如 \u9ad8 -> 高） */
    private String decodeUnicode(String str) {
        if (str == null || str.isEmpty()) return str;
        Pattern pattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        Matcher matcher = pattern.matcher(str);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, String.valueOf((char) Integer.parseInt(matcher.group(1), 16)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String getAccessToken() {
        try {
            String urlStr = "https://aip.baidubce.com/oauth/2.0/token"
                    + "?grant_type=client_credentials"
                    + "&client_id=" + apiKey
                    + "&client_secret=" + secretKey;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line);
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readTree(response.toString()).get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("获取OCR Token失败: " + e.getMessage(), e);
        }
    }
}
