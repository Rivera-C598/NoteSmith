package com.notesmith.ai.models;

/**
 * Request model for Gemini API.
 */
public class AIRequest {
    private final String prompt;
    private final int maxTokens;
    private final double temperature;
    
    public AIRequest(String prompt) {
        this(prompt, 1000, 0.7);
    }
    
    public AIRequest(String prompt, int maxTokens, double temperature) {
        this.prompt = prompt;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public int getMaxTokens() {
        return maxTokens;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    /**
     * Convert to JSON for API request.
     */
    public String toJson() {
        return String.format(
            "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}],\"generationConfig\":{\"maxOutputTokens\":%d,\"temperature\":%.1f}}",
            escapeJson(prompt),
            maxTokens,
            temperature
        );
    }
    
    private String escapeJson(String text) {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
