package com.notesmith.similarity;

import com.notesmith.model.Note;
import com.notesmith.util.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Optimized similarity engine with two-stage filtering.
 * 
 * Stage 1: Fast pre-filtering using cheap algorithms (tags, title, basic keywords)
 * Stage 2: Deep analysis on top candidates only
 * 
 * This dramatically reduces computation and API costs.
 */
public class OptimizedSimilarityEngine extends SimilarityEngine {
    
    private static final Logger logger = Logger.getLogger(OptimizedSimilarityEngine.class);
    
    // Stage 1: How many candidates to keep for deep analysis
    private static final int PREFILTER_CANDIDATES = 20;
    
    // Stage 1: Minimum score to even consider (filters obvious non-matches)
    private static final double PREFILTER_THRESHOLD = 0.05;
    
    @Override
    public List<SimilarityResult> findSimilarNotes(Note targetNote, List<Note> allNotes, int topN) {
        logger.info("Starting optimized similarity search for: " + targetNote.getTitle());
        logger.info("Total notes to search: " + allNotes.size());
        
        // Stage 1: Fast pre-filtering
        List<Note> candidates = prefilterCandidates(targetNote, allNotes);
        logger.info("Pre-filter reduced to " + candidates.size() + " candidates");
        
        // Stage 2: Deep analysis on candidates only
        List<SimilarityResult> results = super.findSimilarNotes(targetNote, candidates, topN);
        logger.info("Final results: " + results.size());
        
        return results;
    }
    
    /**
     * Stage 1: Fast pre-filtering using cheap algorithms.
     * Returns top candidates worth analyzing deeply.
     */
    private List<Note> prefilterCandidates(Note targetNote, List<Note> allNotes) {
        List<CandidateScore> scored = new ArrayList<>();
        
        for (Note candidate : allNotes) {
            // Skip self
            if (candidate.getId().equals(targetNote.getId())) {
                continue;
            }
            
            double score = quickScore(targetNote, candidate);
            
            if (score > PREFILTER_THRESHOLD) {
                scored.add(new CandidateScore(candidate, score));
            }
        }
        
        // Sort by quick score and take top N
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        
        return scored.stream()
            .limit(PREFILTER_CANDIDATES)
            .map(cs -> cs.note)
            .collect(Collectors.toList());
    }
    
    /**
     * Quick scoring using only fast, cheap algorithms.
     * Used for pre-filtering before expensive analysis.
     */
    private double quickScore(Note note1, Note note2) {
        // 1. Tag matching (very fast, high signal)
        double tagScore = quickTagSimilarity(note1, note2);
        if (tagScore > 0.5) {
            return tagScore * 0.8; // Strong signal, boost it
        }
        
        // 2. Title similarity (fast, good signal)
        double titleScore = quickTitleSimilarity(note1, note2);
        
        // 3. Basic keyword overlap (fast)
        double keywordScore = quickKeywordOverlap(note1, note2);
        
        // 4. Temporal proximity (instant)
        double temporalScore = quickTemporalScore(note1, note2);
        
        // Weighted combination (optimized for speed)
        return (0.40 * tagScore) +
               (0.30 * titleScore) +
               (0.25 * keywordScore) +
               (0.05 * temporalScore);
    }
    
    private double quickTagSimilarity(Note note1, Note note2) {
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
    
    private double quickTitleSimilarity(Note note1, Note note2) {
        String title1 = note1.getTitle().toLowerCase();
        String title2 = note2.getTitle().toLowerCase();
        
        // Simple word overlap (faster than Levenshtein)
        Set<String> words1 = new HashSet<>(Arrays.asList(title1.split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(title2.split("\\s+")));
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        if (union.isEmpty()) return 0.0;
        
        return (double) intersection.size() / union.size();
    }
    
    private double quickKeywordOverlap(Note note1, Note note2) {
        // Extract only important words (> 4 chars, not too common)
        Set<String> keywords1 = extractKeywords(note1.getContent());
        Set<String> keywords2 = extractKeywords(note2.getContent());
        
        if (keywords1.isEmpty() || keywords2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> intersection = new HashSet<>(keywords1);
        intersection.retainAll(keywords2);
        
        // Jaccard similarity
        Set<String> union = new HashSet<>(keywords1);
        union.addAll(keywords2);
        
        return (double) intersection.size() / union.size();
    }
    
    private Set<String> extractKeywords(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptySet();
        }
        
        return Arrays.stream(text.toLowerCase().split("\\W+"))
            .filter(word -> word.length() > 4) // Only longer words
            .filter(word -> !isCommonWord(word)) // Filter common words
            .limit(50) // Only top 50 words to keep it fast
            .collect(Collectors.toSet());
    }
    
    private static final Set<String> COMMON_WORDS = new HashSet<>(Arrays.asList(
        "about", "after", "before", "being", "could", "every", "first",
        "found", "great", "having", "might", "never", "other", "should",
        "their", "there", "these", "thing", "think", "those", "under",
        "where", "which", "while", "would", "write"
    ));
    
    private boolean isCommonWord(String word) {
        return COMMON_WORDS.contains(word);
    }
    
    private double quickTemporalScore(Note note1, Note note2) {
        long diff = Math.abs(
            note1.getUpdatedAt().toEpochSecond(java.time.ZoneOffset.UTC) -
            note2.getUpdatedAt().toEpochSecond(java.time.ZoneOffset.UTC)
        );
        
        long oneDay = 24 * 3600;
        long sevenDays = 7 * oneDay;
        
        if (diff < oneDay) {
            return 1.0;
        } else if (diff > sevenDays) {
            return 0.0;
        } else {
            return 1.0 - ((double) diff / sevenDays);
        }
    }
    
    /**
     * Helper class for pre-filtering stage.
     */
    private static class CandidateScore {
        final Note note;
        final double score;
        
        CandidateScore(Note note, double score) {
            this.note = note;
            this.score = score;
        }
    }
}
