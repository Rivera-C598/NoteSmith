package com.notesmith.similarity;

import com.notesmith.model.Note;

import java.util.*;

/**
 * N-gram analyzer for finding similar phrases between notes.
 * Uses bigrams (2-word) and trigrams (3-word) sequences.
 */
public class NGramAnalyzer {
    
    public double calculate(Note note1, Note note2) {
        Set<String> bigrams1 = extractNGrams(note1.getContent(), 2);
        Set<String> bigrams2 = extractNGrams(note2.getContent(), 2);
        
        Set<String> trigrams1 = extractNGrams(note1.getContent(), 3);
        Set<String> trigrams2 = extractNGrams(note2.getContent(), 3);
        
        double bigramSim = jaccardSimilarity(bigrams1, bigrams2);
        double trigramSim = jaccardSimilarity(trigrams1, trigrams2);
        
        // Weight trigrams higher (more specific)
        return (0.4 * bigramSim) + (0.6 * trigramSim);
    }
    
    /**
     * Extract n-grams from text.
     */
    private Set<String> extractNGrams(String text, int n) {
        Set<String> ngrams = new HashSet<>();
        
        if (text == null || text.isEmpty()) {
            return ngrams;
        }
        
        String[] words = text.toLowerCase().split("\\W+");
        
        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder ngram = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) ngram.append(" ");
                ngram.append(words[i + j]);
            }
            
            String ngramStr = ngram.toString();
            if (ngramStr.length() > n * 2) { // Filter very short n-grams
                ngrams.add(ngramStr);
            }
        }
        
        return ngrams;
    }
    
    /**
     * Jaccard similarity between two sets.
     */
    private double jaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return (double) intersection.size() / union.size();
    }
}
