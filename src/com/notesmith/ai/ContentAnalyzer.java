package com.notesmith.ai;

import com.notesmith.ai.models.AIResponse;
import com.notesmith.model.Note;
import com.notesmith.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for analyzing note content and suggesting tags.
 */
public class ContentAnalyzer {
    
    private static final Logger logger = Logger.getLogger(ContentAnalyzer.class);
    private final GeminiClient client;
    
    public ContentAnalyzer() {
        this.client = new GeminiClient();
    }
    
    /**
     * Suggest tags for a note based on its content.
     */
    public List<String> suggestTags(Note note) {
        logger.info("Suggesting tags for: " + note.getTitle());
        
        String prompt = String.format(
            "Based on this note content, suggest 3-5 relevant tags. " +
            "Return ONLY the tags as a comma-separated list, nothing else.\n\n" +
            "Title: %s\n" +
            "Content: %s\n\n" +
            "Tags:",
            note.getTitle(),
            truncateContent(note.getContent(), 500)
        );
        
        AIResponse response = client.generateContent(prompt);
        
        List<String> tags = new ArrayList<>();
        
        if (response.isSuccess()) {
            String tagsText = response.getText().trim();
            String[] tagArray = tagsText.split(",");
            
            for (String tag : tagArray) {
                String trimmed = tag.trim().toLowerCase();
                if (!trimmed.isEmpty() && !tags.contains(trimmed)) {
                    tags.add(trimmed);
                }
            }
            
            logger.info("Suggested " + tags.size() + " tags");
        } else {
            logger.error("Failed to suggest tags: " + response.getError());
        }
        
        return tags;
    }
    
    /**
     * Suggest tags based on partial content (as user types).
     */
    public List<String> suggestTagsFromPartialContent(String title, String content) {
        if (content.length() < 50) {
            return new ArrayList<>(); // Too short to analyze
        }
        
        String prompt = String.format(
            "Based on this partial note, suggest 2-3 relevant tags. " +
            "Return ONLY the tags as a comma-separated list.\n\n" +
            "Title: %s\n" +
            "Content: %s\n\n" +
            "Tags:",
            title,
            truncateContent(content, 300)
        );
        
        AIResponse response = client.generateContent(prompt);
        
        List<String> tags = new ArrayList<>();
        
        if (response.isSuccess()) {
            String tagsText = response.getText().trim();
            String[] tagArray = tagsText.split(",");
            
            for (String tag : tagArray) {
                String trimmed = tag.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    tags.add(trimmed);
                }
            }
        }
        
        return tags;
    }
    
    /**
     * Analyze note sentiment (for journal entries).
     */
    public String analyzeSentiment(Note note) {
        logger.info("Analyzing sentiment for: " + note.getTitle());
        
        String prompt = String.format(
            "Analyze the sentiment/mood of this note. " +
            "Respond with one word: positive, negative, neutral, or mixed.\n\n" +
            "Content: %s\n\n" +
            "Sentiment:",
            truncateContent(note.getContent(), 500)
        );
        
        AIResponse response = client.generateContent(prompt);
        
        if (response.isSuccess()) {
            return response.getText().trim().toLowerCase();
        } else {
            return "unknown";
        }
    }
    
    private String truncateContent(String content, int maxLength) {
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}
