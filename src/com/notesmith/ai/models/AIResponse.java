package com.notesmith.ai.models;

/**
 * Response model from Gemini API.
 */
public class AIResponse {
    private final String text;
    private final boolean success;
    private final String error;
    
    public AIResponse(String text) {
        this.text = text;
        this.success = true;
        this.error = null;
    }
    
    public AIResponse(String error, boolean isError) {
        this.text = null;
        this.success = false;
        this.error = error;
    }
    
    public String getText() {
        return text;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getError() {
        return error;
    }
    
    /**
     * Parse JSON response from Gemini API.
     */
    public static AIResponse fromJson(String json) {
        try {
            // Simple JSON parsing for Gemini response
            // Format: {"candidates":[{"content":{"parts":[{"text":"..."}]}}]}
            
            int textStart = json.indexOf("\"text\":\"");
            if (textStart == -1) {
                return new AIResponse("No text found in response", true);
            }
            
            textStart += 8; // Length of "\"text\":\""
            int textEnd = json.indexOf("\"", textStart);
            
            // Handle escaped quotes
            while (textEnd > 0 && json.charAt(textEnd - 1) == '\\') {
                textEnd = json.indexOf("\"", textEnd + 1);
            }
            
            if (textEnd == -1) {
                return new AIResponse("Malformed JSON response", true);
            }
            
            String text = json.substring(textStart, textEnd);
            
            // Unescape JSON
            text = text
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
            
            return new AIResponse(text);
            
        } catch (Exception e) {
            return new AIResponse("Failed to parse response: " + e.getMessage(), true);
        }
    }
}
