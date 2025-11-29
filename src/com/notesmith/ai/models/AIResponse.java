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
            
            // Find the text field - it might be nested
            int textStart = json.indexOf("\"text\"");
            if (textStart == -1) {
                return new AIResponse("No text found in response. Raw: " + json.substring(0, Math.min(200, json.length())), true);
            }
            
            // Find the opening quote after "text":
            int quoteStart = json.indexOf("\"", textStart + 6);
            if (quoteStart == -1) {
                return new AIResponse("Malformed JSON - no opening quote", true);
            }
            
            quoteStart++; // Move past the opening quote
            
            // Find the closing quote, handling escaped quotes
            StringBuilder text = new StringBuilder();
            boolean escaped = false;
            
            for (int i = quoteStart; i < json.length(); i++) {
                char c = json.charAt(i);
                
                if (escaped) {
                    // Handle escape sequences
                    switch (c) {
                        case 'n': text.append('\n'); break;
                        case 'r': text.append('\r'); break;
                        case 't': text.append('\t'); break;
                        case '"': text.append('"'); break;
                        case '\\': text.append('\\'); break;
                        default: text.append(c); break;
                    }
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    // Found the closing quote
                    return new AIResponse(text.toString());
                } else {
                    text.append(c);
                }
            }
            
            return new AIResponse("Incomplete JSON response", true);
            
        } catch (Exception e) {
            return new AIResponse("Failed to parse response: " + e.getMessage(), true);
        }
    }
}
