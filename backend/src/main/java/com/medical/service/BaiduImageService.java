package com.medical.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
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

@Service
public class BaiduImageService {

    private final BaiduTokenService tokenService;

    public BaiduImageService(BaiduTokenService tokenService) {
        this.tokenService = tokenService;
    }

    /** 调用百度图像识别，判断是否为药片 */
    public Map<String, Object> checkIsMedicine(byte[] imageBytes) {
        Map<String, Object> result = new HashMap<>();
        result.put("isMedicine", false);
        result.put("keyword", "");
        result.put("accuracy", 0);

        try {
            String accessToken = tokenService.getAccessToken();
            String url = "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general"
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
            JsonNode results = root.get("result");

            if (results != null && results.isArray()) {
                String[] medicineKeywords = {"药片", "胶囊", "药品", "片剂", "pill", "tablet", "白色药片", "药丸", "药粒"};
                String[] excludeKeywords = {"tablet computer", "pill bottle", "bottle", "手机", "电脑"};

                for (JsonNode item : results) {
                    String keyword = item.get("keyword").asText().toLowerCase();
                    double score = item.get("score").asDouble();

                    boolean isExcluded = false;
                    for (String ex : excludeKeywords) {
                        if (keyword.contains(ex.toLowerCase())) { isExcluded = true; break; }
                    }
                    if (isExcluded) continue;

                    for (String mk : medicineKeywords) {
                        if (keyword.contains(mk.toLowerCase())) {
                            result.put("isMedicine", true);
                            result.put("keyword", item.get("keyword").asText());
                            result.put("accuracy", score);
                            return result;
                        }
                    }

                    // 非药品关键词，但保留最高分作为参考
                    if (((Number) result.get("accuracy")).doubleValue() < score) {
                        result.put("keyword", item.get("keyword").asText());
                        result.put("accuracy", score);
                    }
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("AI图像识别失败: " + e.getMessage(), e);
        }
    }
}
