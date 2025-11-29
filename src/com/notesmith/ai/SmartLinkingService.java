package com.notesmith.ai;

import com.notesmith.ai.models.AIResponse;
import com.notesmith.ai.models.RelatedNote;
import com.notesmith.model.Note;
import com.notesmith.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for finding related notes using AI.
 */
public class SmartLinkingService {
    
    private static final Logger logger = Logger.getLogger(SmartLinkingService.class);
    private final GeminiClient client;
    
    public SmartLinkingService() {
        this.client = new GeminiClient();
    }
    
    /**
     * Find notes related to the given note.
     * Returns top 5 most related notes with similarity scores.
     */
    public List<RelatedNote> findRelatedNotes(Note currentNote, List<Note> allNotes) {
        List<RelatedNote> relatedNotes = new ArrayList<>();
        
        if (allNotes.isEmpty() || allNotes.size() == 1) {
            return relatedNotes;
        }
        
        logger.info("Finding related notes for: " + currentNote.getTitle());
        
        // Extract keywords from current note
        String keywords = extractKeywords(currentNote);
        if (keywords == null || keywords.isEmpty()) {
            logger.warn("Failed to extract keywords");
            return relatedNotes;
        }
        
        logger.info("Extracted keywords: " + keywords);
        
        // Compare with other notes
        for (Note note : allNotes) {
            if (note.getId().equals(currentNote.getId())) {
                continue; // Skip the current note
            }
            
            double similarity = calculateSimilarity(keywords, note);
            
            if (similarity > 0.3) { // Threshold: 30% similarity
                String reason = generateReason(keywords, note);
                relatedNotes.add(new RelatedNote(note, similarity, reason));
            }
        }
        
        // Sort by similarity (highest first) and take top 5
        relatedNotes.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));
        
        if (relatedNotes.size() > 5) {
            relatedNotes = relatedNotes.subList(0, 5);
        }
        
        logger.info("Found " + relatedNotes.size() + " related notes");
        return relatedNotes;
    }
    
    /**
     * Extract keywords from a note using AI.
     */
    private String extractKeywords(Note note) {
        String prompt = String.format(
            "Extract 5-10 key concepts, topics, or keywords from this note. " +
            "Return ONLY the keywords as a comma-separated list, nothing else.\n\n" +
            "Title: %s\n" +
            "Content: %s\n\n" +
            "Keywords:",
            note.getTitle(),
            truncateContent(note.getContent(), 500)
        );
        
        AIResponse response = client.generateContent(prompt);
        
        if (response.isSuccess()) {
            return response.getText().trim();
        } else {
            logger.error("Failed to extract keywords: " + response.getError());
            return null;
        }
    }
    
    /**
     * Calculate similarity between keywords and a note.
     */
    private double calculateSimilarity(String keywords, Note note) {
        String[] keywordArray = keywords.toLowerCase().split(",");
        String noteText = (note.getTitle() + " " + note.getContent()).toLowerCase();
        
        int matches = 0;
        for (String keyword : keywordArray) {
            String trimmed = keyword.trim();
            if (!trimmed.isEmpty() && noteText.contains(trimmed)) {
                matches++;
            }
        }
        
        // Also check tags
        for (String tag : note.getTags()) {
            for (String keyword : keywordArray) {
                if (tag.toLowerCase().contains(keyword.trim().toLowerCase())) {
                    matches++;
                    break;
                }
            }
        }
        
        return Math.min(1.0, (double) matches / keywordArray.length);
    }
    
    /**
     * Generate a reason why notes are related.
     */
    private String generateReason(String keywords, Note note) {
        String[] keywordArray = keywords.toLowerCase().split(",");
        List<String> matchedKeywords = new ArrayList<>();
        
        String noteText = (note.getTitle() + " " + note.getContent()).toLowerCase();
        
        for (String keyword : keywordArray) {
            String trimmed = keyword.trim();
            if (!trimmed.isEmpty() && noteText.contains(trimmed)) {
                matchedKeywords.add(trimmed);
                if (matchedKeywords.size() >= 3) break; // Limit to 3 keywords
            }
        }
        
        if (matchedKeywords.isEmpty()) {
            return "Similar content";
        }
        
        return "Shares: " + String.join(", ", matchedKeywords);
    }
    
    /**
     * Truncate content to max length.
     */
    private String truncateContent(String content, int maxLength) {
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}
