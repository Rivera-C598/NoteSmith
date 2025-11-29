package com.notesmith.ai;

import com.notesmith.ai.models.AIResponse;
import com.notesmith.model.Note;
import com.notesmith.util.Logger;

/**
 * Service for summarizing notes using AI.
 */
public class SummarizationService {
    
    private static final Logger logger = Logger.getLogger(SummarizationService.class);
    private final GeminiClient client;
    
    public SummarizationService() {
        this.client = new GeminiClient();
    }
    
    /**
     * Generate a summary of the note.
     */
    public String summarize(Note note) {
        logger.info("Summarizing note: " + note.getTitle());
        
        // Use mock mode if no API key configured
        if (MockAIService.shouldUseMockMode(com.notesmith.config.AppConfig.getGeminiApiKey())) {
            logger.info("Using mock AI for summary (no API key configured)");
            return MockAIService.generateMockSummary(note);
        }
        
        String prompt = String.format(
            "Summarize this note in 2-3 concise sentences. Focus on the main points and key takeaways.\n\n" +
            "Title: %s\n" +
            "Content: %s\n\n" +
            "Summary:",
            note.getTitle(),
            note.getContent()
        );
        
        AIResponse response = client.generateContent(prompt);
        
        if (response.isSuccess()) {
            logger.info("Summary generated successfully");
            return response.getText().trim();
        } else {
            logger.error("Failed to generate summary: " + response.getError());
            return "Failed to generate summary: " + response.getError();
        }
    }
    
    /**
     * Generate bullet points from note content.
     */
    public String generateBulletPoints(Note note) {
        logger.info("Generating bullet points for: " + note.getTitle());
        
        String prompt = String.format(
            "Extract the main points from this note as a bullet list (3-5 points).\n\n" +
            "Content: %s\n\n" +
            "Bullet points:",
            note.getContent()
        );
        
        AIResponse response = client.generateContent(prompt);
        
        if (response.isSuccess()) {
            return response.getText().trim();
        } else {
            return "Failed to generate bullet points: " + response.getError();
        }
    }
    
    /**
     * Extract action items from note.
     */
    public String extractActionItems(Note note) {
        logger.info("Extracting action items from: " + note.getTitle());
        
        String prompt = String.format(
            "Extract any action items, tasks, or to-dos from this note. " +
            "Return as a numbered list. If there are no action items, return 'No action items found'.\n\n" +
            "Content: %s\n\n" +
            "Action items:",
            note.getContent()
        );
        
        AIResponse response = client.generateContent(prompt);
        
        if (response.isSuccess()) {
            return response.getText().trim();
        } else {
            return "Failed to extract action items: " + response.getError();
        }
    }
}
