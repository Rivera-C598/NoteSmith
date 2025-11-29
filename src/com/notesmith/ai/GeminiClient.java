package com.notesmith.ai;

import com.notesmith.ai.models.AIRequest;
import com.notesmith.ai.models.AIResponse;
import com.notesmith.config.AppConfig;
import com.notesmith.util.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * HTTP client for Google Gemini API.
 */
public class GeminiClient {
    
    private static final Logger logger = Logger.getLogger(GeminiClient.class);
    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    
    private final String apiKey;
    private final String model;
    
    public GeminiClient() {
        this.apiKey = AppConfig.getGeminiApiKey();
        this.model = AppConfig.getGeminiModel();
        
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("Gemini API key not configured");
        }
    }
    
    /**
     * Send a prompt to Gemini and get response.
     */
    public AIResponse generateContent(String prompt) {
        return generateContent(new AIRequest(prompt));
    }
    
    /**
     * Send a request to Gemini API.
     */
    public AIResponse generateContent(AIRequest request) {
        if (!AppConfig.isAIEnabled()) {
            return new AIResponse("AI features are disabled", true);
        }
        
        if (apiKey == null || apiKey.isEmpty()) {
            return new AIResponse("Gemini API key not configured. Please add it to config.properties", true);
        }
        
        try {
            String endpoint = API_BASE_URL + model + ":generateContent?key=" + apiKey;
            URL url = new URL(endpoint);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000); // 10 seconds
            conn.setReadTimeout(30000); // 30 seconds
            
            // Send request
            String jsonRequest = request.toJson();
            logger.info("Sending request to Gemini API");
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Read response
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                logger.info("Received response from Gemini API");
                return AIResponse.fromJson(response.toString());
                
            } else {
                // Read error response
                StringBuilder error = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        error.append(line);
                    }
                }
                
                logger.error("Gemini API error: " + responseCode + " - " + error.toString());
                return new AIResponse("API Error: " + responseCode + " - " + error.toString(), true);
            }
            
        } catch (Exception e) {
            logger.error("Failed to call Gemini API", e);
            return new AIResponse("Failed to connect to AI service: " + e.getMessage(), true);
        }
    }
    
    /**
     * Test API connectivity.
     */
    public boolean testConnection() {
        AIResponse response = generateContent("Hello, respond with 'OK' if you can read this.");
        return response.isSuccess();
    }
}
