package com.notesmith.similarity;

import com.notesmith.model.Note;

import java.util.Map;

/**
 * Result of similarity calculation between notes.
 */
public class SimilarityResult {
    private final Note note;
    private final double score;
    private final Map<String, Double> reasonBreakdown;
    
    public SimilarityResult(Note note, double score, Map<String, Double> reasonBreakdown) {
        this.note = note;
        this.score = score;
        this.reasonBreakdown = reasonBreakdown;
    }
    
    public Note getNote() {
        return note;
    }
    
    public double getScore() {
        return score;
    }
    
    public Map<String, Double> getReasonBreakdown() {
        return reasonBreakdown;
    }
    
    /**
     * Get human-readable explanation of similarity.
     */
    public String getExplanation() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%.1f%% similar", score * 100));
        
        // Find top reasons
        reasonBreakdown.entrySet().stream()
            .filter(e -> e.getValue() > 0.3)
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(2)
            .forEach(e -> sb.append(String.format(" (%s: %.0f%%)", e.getKey(), e.getValue() * 100)));
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return note.getTitle() + " - " + getExplanation();
    }
}
