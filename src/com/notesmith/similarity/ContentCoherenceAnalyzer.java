package com.notesmith.similarity;

import com.notesmith.model.Note;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes the overall "thought" or theme of note content.
 * Goes beyond keyword matching to understand the conceptual coherence.
 */
public class ContentCoherenceAnalyzer {
    
    /**
     * Calculate how similar the overall "thought" or theme is between two notes.
     * This looks at:
     * - Sentence structure patterns
     * - Conceptual density (how focused the content is)
     * - Semantic flow (how ideas connect)
     * - Writing style similarity
     */
    public double calculateThoughtSimilarity(Note note1, Note note2) {
        double sentencePatternScore = analyzeSentencePatterns(note1, note2);
        double conceptDensityScore = analyzeConceptualDensity(note1, note2);
        double semanticFlowScore = analyzeSemanticFlow(note1, note2);
        double styleScore = analyzeWritingStyle(note1, note2);
        
        // Weighted combination
        return (0.30 * sentencePatternScore) +
               (0.25 * conceptDensityScore) +
               (0.25 * semanticFlowScore) +
               (0.20 * styleScore);
    }
    
    /**
     * Analyze sentence structure patterns.
     * Notes with similar thought patterns often have similar sentence structures.
     */
    private double analyzeSentencePatterns(Note note1, Note note2) {
        List<String> sentences1 = extractSentences(note1.getContent());
        List<String> sentences2 = extractSentences(note2.getContent());
        
        if (sentences1.isEmpty() || sentences2.isEmpty()) {
            return 0.0;
        }
        
        // Compare average sentence length
        double avgLen1 = sentences1.stream().mapToInt(String::length).average().orElse(0);
        double avgLen2 = sentences2.stream().mapToInt(String::length).average().orElse(0);
        double lengthSimilarity = 1.0 - Math.min(1.0, Math.abs(avgLen1 - avgLen2) / Math.max(avgLen1, avgLen2));
        
        // Compare sentence count (similar depth of thought)
        double countRatio = Math.min(sentences1.size(), sentences2.size()) / 
                           (double) Math.max(sentences1.size(), sentences2.size());
        
        // Compare question vs statement ratio (similar inquiry style)
        double questionRatio1 = countQuestions(sentences1) / (double) sentences1.size();
        double questionRatio2 = countQuestions(sentences2) / (double) sentences2.size();
        double questionSimilarity = 1.0 - Math.abs(questionRatio1 - questionRatio2);
        
        return (0.4 * lengthSimilarity) + (0.3 * countRatio) + (0.3 * questionSimilarity);
    }
    
    /**
     * Analyze conceptual density - how focused and coherent the thought is.
     * Notes about similar concepts will have similar vocabulary diversity.
     */
    private double analyzeConceptualDensity(Note note1, Note note2) {
        Set<String> uniqueWords1 = getUniqueWords(note1.getContent());
        Set<String> uniqueWords2 = getUniqueWords(note2.getContent());
        
        int totalWords1 = countWords(note1.getContent());
        int totalWords2 = countWords(note2.getContent());
        
        if (totalWords1 == 0 || totalWords2 == 0) {
            return 0.0;
        }
        
        // Vocabulary diversity ratio (unique words / total words)
        double diversity1 = uniqueWords1.size() / (double) totalWords1;
        double diversity2 = uniqueWords2.size() / (double) totalWords2;
        
        // Similar diversity = similar conceptual focus
        double diversitySimilarity = 1.0 - Math.abs(diversity1 - diversity2);
        
        // Word repetition patterns (focused thought repeats key concepts)
        double repetitionScore = analyzeRepetitionPatterns(note1.getContent(), note2.getContent());
        
        return (0.6 * diversitySimilarity) + (0.4 * repetitionScore);
    }
    
    /**
     * Analyze semantic flow - how ideas connect and build on each other.
     * Similar thought processes have similar flow patterns.
     */
    private double analyzeSemanticFlow(Note note1, Note note2) {
        List<String> sentences1 = extractSentences(note1.getContent());
        List<String> sentences2 = extractSentences(note2.getContent());
        
        if (sentences1.size() < 2 || sentences2.size() < 2) {
            return 0.0;
        }
        
        // Analyze transition words (however, therefore, additionally, etc.)
        double transitionScore = compareTransitionWords(sentences1, sentences2);
        
        // Analyze topic continuity (how much vocabulary carries over between sentences)
        double continuityScore1 = calculateTopicContinuity(sentences1);
        double continuityScore2 = calculateTopicContinuity(sentences2);
        double continuitySimilarity = 1.0 - Math.abs(continuityScore1 - continuityScore2);
        
        return (0.5 * transitionScore) + (0.5 * continuitySimilarity);
    }
    
    /**
     * Analyze writing style similarity.
     * Similar thoughts often expressed in similar styles.
     */
    private double analyzeWritingStyle(Note note1, Note note2) {
        String content1 = note1.getContent();
        String content2 = note2.getContent();
        
        // Punctuation density (formal vs casual)
        double punctDensity1 = countPunctuation(content1) / (double) Math.max(1, content1.length());
        double punctDensity2 = countPunctuation(content2) / (double) Math.max(1, content2.length());
        double punctSimilarity = 1.0 - Math.abs(punctDensity1 - punctDensity2) * 100;
        
        // Capitalization patterns (formal vs casual)
        double capRatio1 = countCapitalLetters(content1) / (double) Math.max(1, content1.length());
        double capRatio2 = countCapitalLetters(content2) / (double) Math.max(1, content2.length());
        double capSimilarity = 1.0 - Math.abs(capRatio1 - capRatio2) * 10;
        
        // List/bullet usage (structured vs narrative)
        boolean hasList1 = content1.contains("- ") || content1.contains("* ") || content1.contains("1.");
        boolean hasList2 = content2.contains("- ") || content2.contains("* ") || content2.contains("1.");
        double listSimilarity = (hasList1 == hasList2) ? 1.0 : 0.3;
        
        return (0.4 * punctSimilarity) + (0.3 * capSimilarity) + (0.3 * listSimilarity);
    }
    
    // ===== Helper Methods =====
    
    private List<String> extractSentences(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(text.split("[.!?]+"))
            .map(String::trim)
            .filter(s -> s.length() > 10) // Filter very short fragments
            .collect(Collectors.toList());
    }
    
    private int countQuestions(List<String> sentences) {
        return (int) sentences.stream()
            .filter(s -> s.trim().endsWith("?"))
            .count();
    }
    
    private Set<String> getUniqueWords(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays.stream(text.toLowerCase().split("\\W+"))
            .filter(w -> w.length() > 2)
            .collect(Collectors.toSet());
    }
    
    private int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.split("\\s+").length;
    }
    
    private double analyzeRepetitionPatterns(String text1, String text2) {
        Map<String, Integer> freq1 = getWordFrequency(text1);
        Map<String, Integer> freq2 = getWordFrequency(text2);
        
        // Find words that are repeated (appear more than once)
        Set<String> repeated1 = freq1.entrySet().stream()
            .filter(e -> e.getValue() > 1)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        
        Set<String> repeated2 = freq2.entrySet().stream()
            .filter(e -> e.getValue() > 1)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        
        if (repeated1.isEmpty() || repeated2.isEmpty()) {
            return 0.0;
        }
        
        // Jaccard similarity of repeated words
        Set<String> intersection = new HashSet<>(repeated1);
        intersection.retainAll(repeated2);
        
        Set<String> union = new HashSet<>(repeated1);
        union.addAll(repeated2);
        
        return (double) intersection.size() / union.size();
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
    
    private static final Set<String> TRANSITION_WORDS = new HashSet<>(Arrays.asList(
        "however", "therefore", "thus", "hence", "moreover", "furthermore",
        "additionally", "consequently", "meanwhile", "nevertheless", "nonetheless",
        "although", "though", "whereas", "while", "because", "since", "if",
        "then", "so", "yet", "still", "also", "besides", "indeed", "in fact"
    ));
    
    private double compareTransitionWords(List<String> sentences1, List<String> sentences2) {
        int transitions1 = countTransitions(sentences1);
        int transitions2 = countTransitions(sentences2);
        
        if (transitions1 == 0 && transitions2 == 0) {
            return 1.0; // Both have no transitions
        }
        
        double ratio = Math.min(transitions1, transitions2) / 
                      (double) Math.max(transitions1, transitions2);
        return ratio;
    }
    
    private int countTransitions(List<String> sentences) {
        return (int) sentences.stream()
            .flatMap(s -> Arrays.stream(s.toLowerCase().split("\\s+")))
            .filter(TRANSITION_WORDS::contains)
            .count();
    }
    
    private double calculateTopicContinuity(List<String> sentences) {
        if (sentences.size() < 2) {
            return 0.0;
        }
        
        double totalContinuity = 0.0;
        
        for (int i = 0; i < sentences.size() - 1; i++) {
            Set<String> words1 = getUniqueWords(sentences.get(i));
            Set<String> words2 = getUniqueWords(sentences.get(i + 1));
            
            Set<String> intersection = new HashSet<>(words1);
            intersection.retainAll(words2);
            
            Set<String> union = new HashSet<>(words1);
            union.addAll(words2);
            
            if (!union.isEmpty()) {
                totalContinuity += (double) intersection.size() / union.size();
            }
        }
        
        return totalContinuity / (sentences.size() - 1);
    }
    
    private int countPunctuation(String text) {
        if (text == null) return 0;
        return (int) text.chars()
            .filter(c -> ".,;:!?-()[]{}\"'".indexOf(c) >= 0)
            .count();
    }
    
    private int countCapitalLetters(String text) {
        if (text == null) return 0;
        return (int) text.chars()
            .filter(Character::isUpperCase)
            .count();
    }
}
