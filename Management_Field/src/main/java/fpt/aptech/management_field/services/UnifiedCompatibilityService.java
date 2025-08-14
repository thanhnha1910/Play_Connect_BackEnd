package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.OpenMatchDto;
import fpt.aptech.management_field.payload.dtos.DraftMatchDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Unified service for calculating compatibility scores across different match types.
 * This service provides a consistent interface for AI-only scoring with no fallback logic.
 */
@Service
public class UnifiedCompatibilityService {
    
    private static final Logger logger = LoggerFactory.getLogger(UnifiedCompatibilityService.class);
    
    @Autowired
    private AIRecommendationService aiRecommendationService;
    
    /**
     * Calculate compatibility scores for teammate recommendations with AI-only approach
     */
    public List<Map<String, Object>> calculateTeammateCompatibility(User user, List<User> potentialTeammates, String sportType) {
        try {
            logger.info("[UNIFIED_COMPATIBILITY] Calculating teammate compatibility for user {} with {} potential teammates - AI ONLY", 
                       user.getId(), potentialTeammates.size());
            
            // Use AI recommendation service only
            List<Map<String, Object>> result = aiRecommendationService.recommendTeammatesHybrid(user, potentialTeammates, sportType);
            
            if (result != null) {
                // Trust AI service results completely, including low scores and empty lists
                logger.info("[AI_ONLY] Successfully received {} teammate recommendations from AI service", result.size());
                
                // Only normalize scores to valid range - no validation logic
                for (Map<String, Object> teammate : result) {
                    Double score = getScoreFromMap(teammate, "compatibilityScore");
                    if (score != null) {
                        teammate.put("compatibilityScore", normalizeScore(score));
                        teammate.put("aiScoreUsed", true);
                        logger.debug("[AI_SCORE_TRUST] Using AI score {} for teammate {}", 
                                   score, teammate.get("userId"));
                    }
                }
                
                logger.info("[AI_ONLY] Trusting AI scores completely - no fallback logic");
                return result;
            }
            
            // No fallback - return empty list when AI service returns null
            logger.warn("[AI_ONLY] AI service returned null result for teammates - returning empty list (no fallback)");
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("[AI_ONLY] AI service failed with exception: {} - returning empty list (no fallback)", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Calculate compatibility scores for open match ranking with AI-only approach
     */
    public List<OpenMatchDto> calculateOpenMatchCompatibility(User user, List<OpenMatchDto> openMatches, String sportType) {
        try {
            logger.info("[UNIFIED_COMPATIBILITY] Calculating open match compatibility for user {} with {} open matches - AI ONLY", 
                       user.getId(), openMatches.size());
            
            // Use AI ranking service only
            List<OpenMatchDto> result = aiRecommendationService.rankOpenMatchesHybrid(user, openMatches, sportType);
            
            if (result != null) {
                // Trust AI service results completely, including low scores and empty lists
                logger.info("[AI_ONLY] Successfully received {} ranked open matches from AI service", result.size());
                
                // Only normalize scores to valid range - no fallback logic
                for (OpenMatchDto match : result) {
                    if (match.getCompatibilityScore() != null) {
                        match.setCompatibilityScore(normalizeScore(match.getCompatibilityScore()));
                        match.setAiScoreUsed(true);
                        logger.debug("[AI_SCORE_TRUST] Using AI score {} for open match {}", 
                                   match.getCompatibilityScore(), match.getId());
                    }
                    if (match.getExplicitScore() != null) {
                        match.setExplicitScore(normalizeScore(match.getExplicitScore()));
                    }
                    if (match.getImplicitScore() != null) {
                        match.setImplicitScore(normalizeScore(match.getImplicitScore()));
                    }
                }
                
                logger.info("[AI_ONLY] Trusting AI scores completely - no fallback logic");
                return result;
            }
            
            // No fallback - return empty list when AI service returns null
            logger.warn("[AI_ONLY] AI service returned null result for open matches - returning empty list (no fallback)");
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("[AI_ONLY] AI service failed with exception: {} - returning empty list (no fallback)", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Calculate compatibility scores for draft matches with AI-only approach
     */
    public List<DraftMatchDto> calculateDraftMatchCompatibility(User user, List<DraftMatchDto> matches, String sportType) {
        try {
            logger.info("[UNIFIED_COMPATIBILITY] Calculating draft match compatibility for user {} with {} matches - AI ONLY", 
                       user.getId(), matches.size());
            
            // Use AI ranking service only
            List<DraftMatchDto> result = aiRecommendationService.rankDraftMatchesHybrid(user, matches, sportType);
            
            if (result != null) {
                // Trust AI service results completely, including low scores and empty lists
                logger.info("[AI_ONLY] Successfully received {} ranked draft matches from AI service", result.size());
                
                // Only normalize scores to valid range - no fallback logic
                for (DraftMatchDto match : result) {
                    if (match.getCompatibilityScore() != null) {
                        match.setCompatibilityScore(normalizeScore(match.getCompatibilityScore()));
                        match.setAiScoreUsed(true);
                        logger.debug("[AI_SCORE_TRUST] Using AI score {} for draft match {}", 
                                   match.getCompatibilityScore(), match.getId());
                    }
                    if (match.getExplicitScore() != null) {
                        match.setExplicitScore(normalizeScore(match.getExplicitScore()));
                    }
                    if (match.getImplicitScore() != null) {
                        match.setImplicitScore(normalizeScore(match.getImplicitScore()));
                    }
                }
                
                logger.info("[AI_ONLY] Trusting AI scores completely - no fallback logic");
                return result;
            }
            
            // No fallback - return empty list when AI service returns null
            logger.warn("[AI_ONLY] AI service returned null result for draft matches - returning empty list (no fallback)");
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("[AI_ONLY] AI service failed with exception: {} - returning empty list (no fallback)", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    // No fallback logic - AI-only service with graceful failure handling

    /**
     * Normalize score to ensure it's within valid range [0.0, 1.0]
     */
    private Double normalizeScore(Double score) {
        if (score == null) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    /**
     * Helper method to safely get score from map
     */
    private Double getScoreFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    /**
     * Get explanation of scoring methodology
     */
    public String getScoreExplanation(String activityLevel) {
        switch (activityLevel != null ? activityLevel.toLowerCase() : "moderate") {
            case "high":
                return "High activity users: 70% compatibility + 20% explicit + 10% implicit";
            case "low":
                return "Low activity users: 50% compatibility + 30% explicit + 20% implicit";
            default:
                return "Moderate activity users: 60% compatibility + 25% explicit + 15% implicit";
        }
    }
}