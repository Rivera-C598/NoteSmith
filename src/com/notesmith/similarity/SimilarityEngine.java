package com.notesmith.similarity;

import com.notesmith.model.Note;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hybrid similarity engine combining multiple algorithms.
 * Works offline with optional AI enhancement.
 */
public class SimilarityEngine {
    
    private final TFIDFCalculator tfidfCalculator;
    private final CosineSimilarity cosineSimilarity;
    private final NGramAnalyzer ngramAnalyzer;
    private final ContentCoherenceAnalyzer coherenceAnalyzer;
    
    // Weights for different similarity signals
    private static final double WEIGHT_TFIDF = 0.20;        // Reduced to make room
    private static final double WEIGHT_JACCARD = 0.12;      // Reduced
    private static final double WEIGHT_COSINE = 0.15;       // Reduced
    private static final double WEIGHT_NGRAM = 0.08;        // Reduced
    private static final double WEIGHT_TAG = 0.15;          // Keep same (important)
    private static final double WEIGHT_TEMPORAL = 0.05;     // Keep same
    private static final double WEIGHT_TITLE = 0.10;        // Keep same
    private static final double WEIGHT_COHERENCE = 0.15;    // NEW: Overall thought similarity
    
    public SimilarityEngine() {
        this.tfidfCalculator = new TFIDFCalculator();
        this.cosineSimilarity = new CosineSimilarity();
        this.ngramAnalyzer = new NGramAnalyzer();
        this.coherenceAnalyzer = new ContentCoherenceAnalyzer();
    }
    
    /**
     * Find similar notes using hybrid algorithm.
     * Returns list of notes sorted by similarity score (highest first).
     */
    public List<SimilarityResult> findSimilarNotes(Note targetNote, List<Note> allNotes, int topN) {
        List<SimilarityResult> results = new ArrayList<>();
        
        // Preprocess all notes for TF-IDF
        tfidfCalculator.buildCorpus(allNotes);
        
        for (Note candidate : allNotes) {
            // Skip self
            if (candidate.getId().equals(targetNote.getId())) {
                continue;
            }
            
            double score = calculateSimilarity(targetNote, candidate);
            
            if (score > 0.1) { // Threshold to filter noise
                results.add(new SimilarityResult(candidate, score, getReasonBreakdown(targetNote, candidate)));
            }
        }
        
        // Sort by score descending
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        // Return top N
        return results.stream().limit(topN).collect(Collectors.toList());
    }
    
    /**
     * Calculate overall similarity score between two notes.
     */
    private double calculateSimilarity(Note note1, Note note2) {
        double tfidfScore = tfidfCalculator.calculate(note1, note2);
        double jaccardScore = calculateJaccardSimilarity(note1, note2);
        double cosineScore = cosineSimilarity.calculate(note1, note2);
        double ngramScore = ngramAnalyzer.calculate(note1, note2);
        double tagScore = calculateTagSimilarity(note1, note2);
        double temporalScore = calculateTemporalProximity(note1, note2);
        double titleScore = calculateTitleSimilarity(note1, note2);
        double coherenceScore = coherenceAnalyzer.calculateThoughtSimilarity(note1, note2);
        
        return (WEIGHT_TFIDF * tfidfScore) +
               (WEIGHT_JACCARD * jaccardScore) +
               (WEIGHT_COSINE * cosineScore) +
               (WEIGHT_NGRAM * ngramScore) +
               (WEIGHT_TAG * tagScore) +
               (WEIGHT_TEMPORAL * temporalScore) +
               (WEIGHT_TITLE * titleScore) +
               (WEIGHT_COHERENCE * coherenceScore);
    }
    
    /**
     * Jaccard similarity: intersection / union of word sets.
     */
    private double calculateJaccardSimilarity(Note note1, Note note2) {
        Set<String> words1 = tokenize(note1.getContent());
        Set<String> words2 = tokenize(note2.getContent());
        
        if (words1.isEmpty() || words2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * Tag-based similarity: percentage of shared tags.
     */
    private double calculateTagSimilarity(Note note1, Note note2) {
        Set<String> tags1 = new HashSet<>(note1.getTags());
        Set<String> tags2 = new HashSet<>(note2.getTags());
        
        if (tags1.isEmpty() || tags2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> intersection = new HashSet<>(tags1);
        intersection.retainAll(tags2);
        
        Set<String> union = new HashSet<>(tags1);
        union.addAll(tags2);
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * Temporal proximity: notes created/edited close in time.
     */
    private double calculateTemporalProximity(Note note1, Note note2) {
        long diff = Math.abs(
            note1.getUpdatedAt().toEpochSecond(java.time.ZoneOffset.UTC) -
            note2.getUpdatedAt().toEpochSecond(java.time.ZoneOffset.UTC)
        );
        
        // Within 1 hour = 1.0, decay over 7 days
        long oneHour = 3600;
        long sevenDays = 7 * 24 * 3600;
        
        if (diff < oneHour) {
            return 1.0;
        } else if (diff > sevenDays) {
            return 0.0;
        } else {
            return 1.0 - ((double) diff / sevenDays);
        }
    }
    
    /**
     * Title similarity using Levenshtein distance.
     */
    private double calculateTitleSimilarity(Note note1, Note note2) {
        String title1 = note1.getTitle().toLowerCase();
        String title2 = note2.getTitle().toLowerCase();
        
        int distance = levenshteinDistance(title1, title2);
        int maxLen = Math.max(title1.length(), title2.length());
        
        if (maxLen == 0) {
            return 0.0;
        }
        
        return 1.0 - ((double) distance / maxLen);
    }
    
    /**
     * Levenshtein distance (edit distance) between two strings.
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Get breakdown of why notes are similar.
     */
    private Map<String, Double> getReasonBreakdown(Note note1, Note note2) {
        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("TF-IDF", tfidfCalculator.calculate(note1, note2));
        breakdown.put("Jaccard", calculateJaccardSimilarity(note1, note2));
        breakdown.put("Cosine", cosineSimilarity.calculate(note1, note2));
        breakdown.put("N-gram", ngramAnalyzer.calculate(note1, note2));
        breakdown.put("Tags", calculateTagSimilarity(note1, note2));
        breakdown.put("Temporal", calculateTemporalProximity(note1, note2));
        breakdown.put("Title", calculateTitleSimilarity(note1, note2));
        breakdown.put("Thought/Theme", coherenceAnalyzer.calculateThoughtSimilarity(note1, note2));
        return breakdown;
    }
    
    /**
     * Tokenize text into words (lowercase, alphanumeric only).
     */
    private Set<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptySet();
        }
        
        return Arrays.stream(text.toLowerCase().split("\\W+"))
            .filter(word -> word.length() > 2) // Filter short words
            .collect(Collectors.toSet());
    }
}
