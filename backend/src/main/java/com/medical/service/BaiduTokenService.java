package com.medical.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

@Service
public class BaiduTokenService {

    @Value("${baidu.ai.api-key}")
    private String apiKey;

    @Value("${baidu.ai.secret-key}")
    private String secretKey;

    private String cachedToken;
    private LocalDateTime tokenExpiry = LocalDateTime.now();

    /** 获取百度AI AccessToken（带缓存，过期前5分钟刷新） */
    public String getAccessToken() {
        if (cachedToken != null && LocalDateTime.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        try {
            String urlStr = "https://aip.baidubce.com/oauth/2.0/token"
                    + "?grant_type=client_credentials"
                    + "&client_id=" + apiKey
                    + "&client_secret=" + secretKey;

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());

            cachedToken = root.get("access_token").asText();
            int expiresIn = root.has("expires_in") ? root.get("expires_in").asInt(2592000) : 2592000;
            // 提前5分钟过期，避免边界情况
            tokenExpiry = LocalDateTime.now().plusSeconds(expiresIn - 300);

            return cachedToken;
        } catch (Exception e) {
            throw new RuntimeException("获取百度AI Token失败: " + e.getMessage(), e);
        }
    }
}
