package com.notesmith.ai.models;

import com.notesmith.model.Note;

/**
 * Represents a note with its similarity score to another note.
 */
public class RelatedNote {
    private final Note note;
    private final double similarityScore;
    private final String reason;
    
    public RelatedNote(Note note, double similarityScore, String reason) {
        this.note = note;
        this.similarityScore = similarityScore;
        this.reason = reason;
    }
    
    public Note getNote() {
        return note;
    }
    
    public double getSimilarityScore() {
        return similarityScore;
    }
    
    public int getSimilarityPercentage() {
        return (int) (similarityScore * 100);
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%d%%) - %s", 
            note.getTitle(), 
            getSimilarityPercentage(), 
            reason);
    }
}
