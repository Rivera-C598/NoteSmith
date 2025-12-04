package com.notesmith.similarity;

import com.notesmith.ai.SmartLinkingService;
import com.notesmith.ai.models.RelatedNote;
import com.notesmith.config.AppConfig;
import com.notesmith.model.Note;
import com.notesmith.util.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hybrid similarity service combining local algorithms with optional AI enhancement.
 * 
 * Strategy:
 * 1. Use local SimilarityEngine for fast, offline similarity detection
 * 2. Optionally enhance with AI for semantic understanding
 * 3. Merge and re-rank results
 */
public class HybridSimilarityService {
    
    private static final Logger logger = Logger.getLogger(HybridSimilarityService.class);
    
    private final SimilarityEngine localEngine;
    private final SmartLinkingService aiService;
    private final boolean aiEnabled;
    
    // Weights for hybrid scoring
    private static final double LOCAL_WEIGHT = 0.7;  // Local algorithms
    private static final double AI_WEIGHT = 0.3;     // AI enhancement
    
    public HybridSimilarityService() {
        this.localEngine = new OptimizedSimilarityEngine(); // Use optimized version
        this.aiEnabled = AppConfig.isAIEnabled();
        this.aiService = aiEnabled ? new SmartLinkingService() : null;
    }
    
    /**
     * Find similar notes using hybrid approach.
     * 
     * @param targetNote The note to find similarities for
     * @param allNotes All available notes
     * @param topN Number of results to return
     * @return List of similar notes with scores and explanations
     */
    public List<SimilarityResult> findSimilarNotes(Note targetNote, List<Note> allNotes, int topN) {
        logger.info("Finding similar notes for: " + targetNote.getTitle());
        
        // Step 1: Get local similarity results (fast, always available)
        List<SimilarityResult> localResults = localEngine.findSimilarNotes(targetNote, allNotes, topN * 2);
        logger.info("Local engine found " + localResults.size() + " candidates");
        
        // Step 2: If AI is enabled, enhance with semantic understanding
        if (aiEnabled && aiService != null) {
            try {
                List<SimilarityResult> enhancedResults = enhanceWithAI(targetNote, localResults, allNotes);
                logger.info("AI enhancement complete");
                return enhancedResults.stream().limit(topN).collect(Collectors.toList());
            } catch (Exception e) {
                logger.error("AI enhancement failed, falling back to local results", e);
                return localResults.stream().limit(topN).collect(Collectors.toList());
            }
        }
        
        // Step 3: Return local results if AI is disabled
        return localResults.stream().limit(topN).collect(Collectors.toList());
    }
    
    /**
     * Enhance local results with AI semantic understanding.
     */
    private List<SimilarityResult> enhanceWithAI(Note targetNote, List<SimilarityResult> localResults, List<Note> allNotes) {
        // OPTIMIZATION: Only send top local candidates to AI (not all notes!)
        // Extract notes from local results + a few extras
        List<Note> candidatesForAI = localResults.stream()
            .map(SimilarityResult::getNote)
            .limit(15) // Only top 15 local results
            .collect(Collectors.toList());
        
        logger.info("Sending " + candidatesForAI.size() + " candidates to AI (not all " + allNotes.size() + " notes)");
        
        // Get AI's perspective on these candidates only
        List<RelatedNote> aiResults = aiService.findRelatedNotes(targetNote, candidatesForAI);
        
        // Create a map of note ID to AI score
        Map<String, Double> aiScores = new HashMap<>();
        Map<String, String> aiReasons = new HashMap<>();
        
        for (RelatedNote relatedNote : aiResults) {
            aiScores.put(relatedNote.getNote().getId(), relatedNote.getSimilarityScore());
            aiReasons.put(relatedNote.getNote().getId(), relatedNote.getReason());
        }
        
        // Merge local and AI scores
        List<SimilarityResult> mergedResults = new ArrayList<>();
        
        for (SimilarityResult localResult : localResults) {
            String noteId = localResult.getNote().getId();
            double localScore = localResult.getScore();
            double aiScore = aiScores.getOrDefault(noteId, 0.0);
            
            // Hybrid score: weighted combination
            double hybridScore = (LOCAL_WEIGHT * localScore) + (AI_WEIGHT * aiScore);
            
            // Add AI reason to breakdown
            Map<String, Double> enhancedBreakdown = new LinkedHashMap<>(localResult.getReasonBreakdown());
            if (aiScore > 0) {
                enhancedBreakdown.put("AI Semantic", aiScore);
                enhancedBreakdown.put("AI Reason", 0.0); // Placeholder for display
            }
            
            mergedResults.add(new SimilarityResult(
                localResult.getNote(),
                hybridScore,
                enhancedBreakdown
            ));
        }
        
        // Also add any AI-discovered notes that local engine missed
        for (RelatedNote aiResult : aiResults) {
            String noteId = aiResult.getNote().getId();
            boolean alreadyIncluded = mergedResults.stream()
                .anyMatch(r -> r.getNote().getId().equals(noteId));
            
            if (!alreadyIncluded) {
                Map<String, Double> breakdown = new LinkedHashMap<>();
                breakdown.put("AI Semantic", aiResult.getSimilarityScore());
                breakdown.put("AI Reason", 0.0);
                
                mergedResults.add(new SimilarityResult(
                    aiResult.getNote(),
                    AI_WEIGHT * aiResult.getSimilarityScore(), // Only AI score
                    breakdown
                ));
            }
        }
        
        // Re-sort by hybrid score
        mergedResults.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        return mergedResults;
    }
    
    /**
     * Get explanation of why notes are similar.
     * Includes both local algorithm reasons and AI insights.
     */
    public String getDetailedExplanation(SimilarityResult result) {
        StringBuilder explanation = new StringBuilder();
        explanation.append(String.format("%.1f%% similar\n\n", result.getScore() * 100));
        
        explanation.append("Similarity Breakdown:\n");
        for (Map.Entry<String, Double> entry : result.getReasonBreakdown().entrySet()) {
            if (entry.getValue() > 0.05) { // Only show significant factors
                explanation.append(String.format("  • %s: %.0f%%\n", 
                    entry.getKey(), entry.getValue() * 100));
            }
        }
        
        return explanation.toString();
    }
    
    /**
     * Check if AI enhancement is available.
     */
    public boolean isAIEnabled() {
        return aiEnabled;
    }
}
