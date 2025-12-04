package com.notesmith.similarity;

import com.notesmith.model.Note;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TF-IDF (Term Frequency-Inverse Document Frequency) calculator.
 * Finds important words and calculates document similarity.
 */
public class TFIDFCalculator {
    
    private Map<String, Integer> documentFrequency;
    private int totalDocuments;
    private Set<String> stopWords;
    
    public TFIDFCalculator() {
        this.documentFrequency = new HashMap<>();
        this.stopWords = createStopWords();
    }
    
    /**
     * Build corpus statistics from all notes.
     */
    public void buildCorpus(List<Note> notes) {
        documentFrequency.clear();
        totalDocuments = notes.size();
        
        for (Note note : notes) {
            Set<String> uniqueWords = tokenize(note.getContent());
            for (String word : uniqueWords) {
                documentFrequency.put(word, documentFrequency.getOrDefault(word, 0) + 1);
            }
        }
    }
    
    /**
     * Calculate TF-IDF similarity between two notes.
     */
    public double calculate(Note note1, Note note2) {
        Map<String, Double> vector1 = getTFIDFVector(note1);
        Map<String, Double> vector2 = getTFIDFVector(note2);
        
        return cosineSimilarity(vector1, vector2);
    }
    
    /**
     * Get TF-IDF vector for a note.
     */
    private Map<String, Double> getTFIDFVector(Note note) {
        Map<String, Double> vector = new HashMap<>();
        List<String> words = tokenizeList(note.getContent());
        
        // Calculate term frequency
        Map<String, Integer> termFreq = new HashMap<>();
        for (String word : words) {
            termFreq.put(word, termFreq.getOrDefault(word, 0) + 1);
        }
        
        // Calculate TF-IDF for each term
        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            String term = entry.getKey();
            int tf = entry.getValue();
            
            double idf = calculateIDF(term);
            double tfidf = tf * idf;
            
            vector.put(term, tfidf);
        }
        
        return vector;
    }
    
    /**
     * Calculate Inverse Document Frequency for a term.
     */
    private double calculateIDF(String term) {
        int df = documentFrequency.getOrDefault(term, 0);
        if (df == 0) {
            return 0.0;
        }
        return Math.log((double) totalDocuments / df);
    }
    
    /**
     * Calculate cosine similarity between two TF-IDF vectors.
     */
    private double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        if (v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(v1.keySet());
        allTerms.addAll(v2.keySet());
        
        for (String term : allTerms) {
            double val1 = v1.getOrDefault(term, 0.0);
            double val2 = v2.getOrDefault(term, 0.0);
            
            dotProduct += val1 * val2;
            norm1 += val1 * val1;
            norm2 += val2 * val2;
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * Tokenize text into set of words.
     */
    private Set<String> tokenize(String text) {
        return tokenizeList(text).stream().collect(Collectors.toSet());
    }
    
    /**
     * Tokenize text into list of words (preserves duplicates).
     */
    private List<String> tokenizeList(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(text.toLowerCase().split("\\W+"))
            .filter(word -> word.length() > 2)
            .filter(word -> !stopWords.contains(word))
            .collect(Collectors.toList());
    }
    
    /**
     * Common English stop words to filter out.
     */
    private Set<String> createStopWords() {
        return new HashSet<>(Arrays.asList(
            "the", "and", "for", "are", "but", "not", "you", "all", "can", "her",
            "was", "one", "our", "out", "day", "get", "has", "him", "his", "how",
            "man", "new", "now", "old", "see", "two", "way", "who", "boy", "did",
            "its", "let", "put", "say", "she", "too", "use", "this", "that", "with",
            "have", "from", "they", "will", "what", "been", "more", "when", "your",
            "said", "each", "tell", "does", "very", "just", "than", "into", "them"
        ));
    }
}
