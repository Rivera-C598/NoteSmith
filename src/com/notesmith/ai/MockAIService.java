package com.notesmith.ai;

import com.notesmith.ai.models.RelatedNote;
import com.notesmith.model.Note;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Mock AI service for testing/demo purposes when no API key is configured.
 * Provides generic responses to simulate AI functionality.
 */
public class MockAIService {
    
    private static final Random random = new Random();
    
    private static final String[] SUMMARY_TEMPLATES = {
        "This note discusses %s and provides insights on %s. Key takeaway: %s.",
        "The main focus is on %s, with emphasis on %s. Important point: %s.",
        "This covers %s topics, particularly %s. Notable mention: %s.",
        "An overview of %s with details about %s. Remember: %s.",
        "Key concepts include %s and %s. Main idea: %s."
    };
    
    private static final String[] TOPICS = {
        "important concepts", "key ideas", "main themes", "core principles",
        "essential points", "fundamental aspects", "critical elements"
    };
    
    private static final String[] INSIGHTS = {
        "practical applications", "theoretical foundations", "real-world examples",
        "best practices", "common patterns", "useful techniques"
    };
    
    private static final String[] TAKEAWAYS = {
        "stay focused on the goal", "practice makes perfect", "consistency is key",
        "attention to detail matters", "keep learning and improving"
    };
    
    private static final String[] TAG_SUGGESTIONS = {
        "important", "review", "ideas", "notes", "reference", "todo",
        "project", "personal", "work", "study", "research", "draft"
    };
    
    /**
     * Generate a mock summary for a note.
     */
    public static String generateMockSummary(Note note) {
        String template = SUMMARY_TEMPLATES[random.nextInt(SUMMARY_TEMPLATES.length)];
        String topic = TOPICS[random.nextInt(TOPICS.length)];
        String insight = INSIGHTS[random.nextInt(INSIGHTS.length)];
        String takeaway = TAKEAWAYS[random.nextInt(TAKEAWAYS.length)];
        
        return String.format(template, topic, insight, takeaway) + 
               "\n\n[Mock AI Response - Configure Gemini API key for real AI features]";
    }
    
    /**
     * Generate mock tag suggestions.
     */
    public static List<String> generateMockTags(Note note) {
        List<String> tags = new ArrayList<>();
        
        // Pick 3-5 random tags
        int count = 3 + random.nextInt(3);
        List<String> available = new ArrayList<>(Arrays.asList(TAG_SUGGESTIONS));
        
        for (int i = 0; i < count && !available.isEmpty(); i++) {
            int index = random.nextInt(available.size());
            tags.add(available.remove(index));
        }
        
        return tags;
    }
    
    /**
     * Find mock related notes using simple keyword matching.
     */
    public static List<RelatedNote> findMockRelatedNotes(Note currentNote, List<Note> allNotes) {
        List<RelatedNote> related = new ArrayList<>();
        
        if (allNotes.size() <= 1) {
            return related;
        }
        
        // Extract words from current note
        String[] words = (currentNote.getTitle() + " " + currentNote.getContent())
            .toLowerCase()
            .split("\\W+");
        
        // Simple keyword matching
        for (Note note : allNotes) {
            if (note.getId().equals(currentNote.getId())) {
                continue;
            }
            
            String noteText = (note.getTitle() + " " + note.getContent()).toLowerCase();
            int matches = 0;
            
            for (String word : words) {
                if (word.length() > 3 && noteText.contains(word)) {
                    matches++;
                }
            }
            
            if (matches > 0) {
                double similarity = Math.min(0.95, matches * 0.15);
                String reason = "Shares " + matches + " keyword" + (matches > 1 ? "s" : "");
                related.add(new RelatedNote(note, similarity, reason));
            }
        }
        
        // Sort by similarity
        related.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));
        
        // Return top 5
        if (related.size() > 5) {
            related = related.subList(0, 5);
        }
        
        return related;
    }
    
    /**
     * Check if mock mode should be used.
     */
    public static boolean shouldUseMockMode(String apiKey) {
        return apiKey == null || apiKey.isEmpty() || 
               apiKey.equals("YOUR_GEMINI_API_KEY_HERE") ||
               apiKey.equals("YOUR_API_KEY_HERE");
    }
}
