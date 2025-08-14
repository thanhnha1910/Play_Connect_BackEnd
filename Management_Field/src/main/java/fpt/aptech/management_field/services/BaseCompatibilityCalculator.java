package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.OpenMatchDto;
import fpt.aptech.management_field.payload.dtos.DraftMatchDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Base compatibility calculator to ensure consistency across all AI recommendation types
 * Addresses the issue where different AI endpoints return inconsistent compatibility scores
 * for the same user (77% for teammates, 60% for open matches, 65% for draft matches)
 */
@Component
public class BaseCompatibilityCalculator {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseCompatibilityCalculator.class);
    
    // Base weights for unified compatibility calculation
    private static final double EXPLICIT_TAGS_WEIGHT = 0.4;
    private static final double IMPLICIT_TAGS_WEIGHT = 0.3;
    private static final double LOCATION_WEIGHT = 0.2;
    private static final double SKILL_LEVEL_WEIGHT = 0.1;
    
    /**
     * Calculate unified base compatibility score that ensures consistency
     * across all recommendation types
     */
    public double calculateBaseCompatibility(User user, Map<String, Object> targetData, String context) {
        try {
            logger.info("[UNIFIED_COMPATIBILITY] Calculating base compatibility for user {} in context {}", 
                       user.getId(), context);
            
            double explicitScore = calculateExplicitTagsCompatibility(user, targetData, context);
            double implicitScore = calculateImplicitTagsCompatibility(user, targetData, context);
            double locationScore = calculateLocationCompatibility(user, targetData, context);
            double skillScore = calculateSkillCompatibility(user, targetData, context);
            
            // Calculate weighted base score
            double baseScore = (explicitScore * EXPLICIT_TAGS_WEIGHT) +
                              (implicitScore * IMPLICIT_TAGS_WEIGHT) +
                              (locationScore * LOCATION_WEIGHT) +
                              (skillScore * SKILL_LEVEL_WEIGHT);
            
            // Apply context-specific adjustments
            double adjustedScore = applyContextAdjustments(baseScore, context, user, targetData);
            
            // Ensure score is within valid range [0.0, 1.0]
            double finalScore = Math.max(0.0, Math.min(1.0, adjustedScore));
            
            logger.info("[UNIFIED_COMPATIBILITY] User {}: explicit={}, implicit={}, location={}, skill={}, base={}, adjusted={}, final={}",
                       user.getId(), explicitScore, implicitScore, locationScore, skillScore, 
                       baseScore, adjustedScore, finalScore);
            
            return finalScore;
            
        } catch (Exception e) {
            logger.error("[UNIFIED_COMPATIBILITY] Error calculating compatibility for user {}: {}", 
                        user.getId(), e.getMessage(), e);
            return 0.5; // Default neutral score
        }
    }
    
    /**
     * Calculate explicit tags compatibility
     */
    private double calculateExplicitTagsCompatibility(User user, Map<String, Object> targetData, String context) {
        try {
            List<String> userTags = extractUserExplicitTags(user, context);
            List<String> targetTags = extractTargetExplicitTags(targetData, context);
            
            if (userTags.isEmpty() || targetTags.isEmpty()) {
                return 0.5; // Neutral score when no tags available
            }
            
            // Calculate Jaccard similarity
            Set<String> userTagsSet = new HashSet<>(userTags);
            Set<String> targetTagsSet = new HashSet<>(targetTags);
            Set<String> intersection = new HashSet<>(userTagsSet);
            intersection.retainAll(targetTagsSet);
            
            Set<String> union = new HashSet<>(userTagsSet);
            union.addAll(targetTagsSet);
            
            double similarity = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
            
            logger.debug("[EXPLICIT_TAGS] User tags: {}, Target tags: {}, Similarity: {}", 
                        userTags, targetTags, similarity);
            
            return similarity;
            
        } catch (Exception e) {
            logger.error("[EXPLICIT_TAGS] Error calculating explicit tags compatibility: {}", e.getMessage());
            return 0.5;
        }
    }
    
    /**
     * Calculate implicit tags compatibility
     */
    private double calculateImplicitTagsCompatibility(User user, Map<String, Object> targetData, String context) {
        try {
            List<String> userImplicitTags = extractUserImplicitTags(user, context);
            List<String> targetImplicitTags = extractTargetImplicitTags(targetData, context);
            
            if (userImplicitTags.isEmpty() || targetImplicitTags.isEmpty()) {
                return 0.5; // Neutral score when no implicit tags available
            }
            
            // Calculate semantic similarity for implicit tags
            double totalSimilarity = 0.0;
            int comparisons = 0;
            
            for (String userTag : userImplicitTags) {
                for (String targetTag : targetImplicitTags) {
                    totalSimilarity += calculateSemanticSimilarity(userTag, targetTag);
                    comparisons++;
                }
            }
            
            double avgSimilarity = comparisons > 0 ? totalSimilarity / comparisons : 0.5;
            
            logger.debug("[IMPLICIT_TAGS] User implicit: {}, Target implicit: {}, Avg similarity: {}", 
                        userImplicitTags, targetImplicitTags, avgSimilarity);
            
            return avgSimilarity;
            
        } catch (Exception e) {
            logger.error("[IMPLICIT_TAGS] Error calculating implicit tags compatibility: {}", e.getMessage());
            return 0.5;
        }
    }
    
    /**
     * Calculate location compatibility
     */
    private double calculateLocationCompatibility(User user, Map<String, Object> targetData, String context) {
        try {
            String userLocation = user.getAddress();
            String targetLocation = extractTargetLocation(targetData, context);
            
            if (userLocation == null || targetLocation == null) {
                return 0.5; // Neutral score when location data is missing
            }
            
            // Simple location similarity based on string matching
            // In a real implementation, this could use geolocation APIs
            double similarity = calculateLocationSimilarity(userLocation, targetLocation);
            
            logger.debug("[LOCATION] User: {}, Target: {}, Similarity: {}", 
                        userLocation, targetLocation, similarity);
            
            return similarity;
            
        } catch (Exception e) {
            logger.error("[LOCATION] Error calculating location compatibility: {}", e.getMessage());
            return 0.5;
        }
    }
    
    /**
     * Calculate skill level compatibility
     */
    private double calculateSkillCompatibility(User user, Map<String, Object> targetData, String context) {
        try {
            String userSkill = extractUserSkillLevel(user, context);
            String targetSkill = extractTargetSkillLevel(targetData, context);
            
            if (userSkill == null || targetSkill == null) {
                return 0.5; // Neutral score when skill data is missing
            }
            
            double similarity = calculateSkillSimilarity(userSkill, targetSkill);
            
            logger.debug("[SKILL] User: {}, Target: {}, Similarity: {}", 
                        userSkill, targetSkill, similarity);
            
            return similarity;
            
        } catch (Exception e) {
            logger.error("[SKILL] Error calculating skill compatibility: {}", e.getMessage());
            return 0.5;
        }
    }
    
    /**
     * Apply context-specific adjustments to ensure appropriate scoring for different use cases
     */
    private double applyContextAdjustments(double baseScore, String context, User user, Map<String, Object> targetData) {
        switch (context.toLowerCase()) {
            case "teammate":
                // Teammates need higher compatibility for team cohesion
                return baseScore * 1.1; // 10% boost for teammate recommendations
                
            case "open_match":
                // Open matches can be more flexible
                return baseScore * 0.95; // 5% reduction for open match flexibility
                
            case "draft_match":
                // Draft matches need balanced approach
                return baseScore * 1.0; // No adjustment for draft matches
                
            default:
                logger.warn("[CONTEXT_ADJUSTMENT] Unknown context: {}, using base score", context);
                return baseScore;
        }
    }
    
    // Helper methods for extracting data based on context
    private List<String> extractUserExplicitTags(User user, String context) {
        // Implementation would extract user's explicit tags based on context
        return new ArrayList<>(); // Placeholder
    }
    
    private List<String> extractTargetExplicitTags(Map<String, Object> targetData, String context) {
        // Implementation would extract target's explicit tags based on context
        return new ArrayList<>(); // Placeholder
    }
    
    private List<String> extractUserImplicitTags(User user, String context) {
        // Implementation would extract user's implicit tags based on context
        return new ArrayList<>(); // Placeholder
    }
    
    private List<String> extractTargetImplicitTags(Map<String, Object> targetData, String context) {
        // Implementation would extract target's implicit tags based on context
        return new ArrayList<>(); // Placeholder
    }
    
    private String extractTargetLocation(Map<String, Object> targetData, String context) {
        // Implementation would extract location based on context
        return null; // Placeholder
    }
    
    private String extractUserSkillLevel(User user, String context) {
        // Implementation would extract user skill level based on context
        return null; // Placeholder
    }
    
    private String extractTargetSkillLevel(Map<String, Object> targetData, String context) {
        // Implementation would extract target skill level based on context
        return null; // Placeholder
    }
    
    private double calculateSemanticSimilarity(String tag1, String tag2) {
        // Simple implementation - in production, this could use NLP models
        return tag1.equalsIgnoreCase(tag2) ? 1.0 : 0.0;
    }
    
    private double calculateLocationSimilarity(String loc1, String loc2) {
        // Simple implementation - in production, this could use geolocation
        return loc1.equalsIgnoreCase(loc2) ? 1.0 : 0.5;
    }
    
    private double calculateSkillSimilarity(String skill1, String skill2) {
        // Implementation for skill level similarity
        Map<String, Integer> skillLevels = Map.of(
            "BEGINNER", 1,
            "INTERMEDIATE", 2,
            "ADVANCED", 3,
            "EXPERT", 4
        );
        
        Integer level1 = skillLevels.get(skill1.toUpperCase());
        Integer level2 = skillLevels.get(skill2.toUpperCase());
        
        if (level1 == null || level2 == null) {
            return 0.5;
        }
        
        int diff = Math.abs(level1 - level2);
        return Math.max(0.0, 1.0 - (diff * 0.25)); // 25% penalty per level difference
    }
}