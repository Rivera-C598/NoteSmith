package com.notesmith.similarity;

import com.notesmith.model.Note;

import java.util.*;

/**
 * Cosine similarity calculator using simple word frequency vectors.
 */
public class CosineSimilarity {
    
    public double calculate(Note note1, Note note2) {
        Map<String, Integer> vector1 = getWordFrequency(note1.getContent());
        Map<String, Integer> vector2 = getWordFrequency(note2.getContent());
        
        return cosineSimilarity(vector1, vector2);
    }
    
    private Map<String, Integer> getWordFrequency(String text) {
        Map<String, Integer> freq = new HashMap<>();
        
        if (text == null || text.isEmpty()) {
            return freq;
        }
        
        String[] words = text.toLowerCase().split("\\W+");
        for (String word : words) {
            if (word.length() > 2) {
                freq.put(word, freq.getOrDefault(word, 0) + 1);
            }
        }
        
        return freq;
    }
    
    private double cosineSimilarity(Map<String, Integer> v1, Map<String, Integer> v2) {
        if (v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> allWords = new HashSet<>();
        allWords.addAll(v1.keySet());
        allWords.addAll(v2.keySet());
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (String word : allWords) {
            int freq1 = v1.getOrDefault(word, 0);
            int freq2 = v2.getOrDefault(word, 0);
            
            dotProduct += freq1 * freq2;
            norm1 += freq1 * freq1;
            norm2 += freq2 * freq2;
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
