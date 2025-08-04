package fpt.aptech.management_field.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.aptech.management_field.payload.dtos.DraftMatchDto;
import fpt.aptech.management_field.payload.dtos.OpenMatchDto;
import fpt.aptech.management_field.payload.dtos.SportProfileDto;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.repositories.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class AIRecommendationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIRecommendationService.class);
    
    @Value("${ai.service.url:http://localhost:5002}")
    private String aiServiceBaseUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    public AIRecommendationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Rank open matches based on user's sport profile tags - SIMPLE VERSION
     */

    

    
    /**
     * Validate recommendation data before processing
     */
    private void validateRecommendationData(User user, List<User> potentialTeammates, String sportType) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        if (sportType == null || sportType.trim().isEmpty()) {
            throw new IllegalArgumentException("Sport type is required for recommendations");
        }
        
        if (user.getSportProfiles() == null || user.getSportProfiles().isEmpty()) {
            throw new IllegalArgumentException("User sport profile is required for recommendations");
        }
        
        List<String> userTags = extractUserTags(user, sportType);
        if (userTags.isEmpty()) {
            logger.warn("User {} has no tags for sport {}", user.getId(), sportType);
        }
        
        if (potentialTeammates == null || potentialTeammates.isEmpty()) {
            throw new IllegalArgumentException("No potential teammates available for recommendation");
        }
        
        logger.info("Validation passed - User: {}, Sport: {}, Tags: {}, Potential teammates: {}", 
                    user.getId(), sportType, userTags.size(), potentialTeammates.size());
    }
    
    /**
     * Fallback recommendation strategy when AI service fails
     */
    private List<Map<String, Object>> fallbackRecommendation(List<User> potentialTeammates) {
        logger.info("Using fallback recommendation strategy");
        return potentialTeammates.stream()
                .limit(3)
                .map(this::convertUserToMap)
                .collect(Collectors.toList());
    }
    
    /**
     * Fallback ranking for open matches when AI ranking service fails
     * Uses simple tag-based compatibility scoring
     */
    public List<OpenMatchDto> fallbackRankOpenMatches(User currentUser, List<OpenMatchDto> matches, String sportType) {
        try {
            logger.info("[FALLBACK_RANKING] Using fallback ranking for {} matches, user: {}, sport: {}", 
                       matches.size(), currentUser.getId(), sportType);
            
            if (matches == null || matches.isEmpty()) {
                return new ArrayList<>();
            }
            
            // Extract user tags for the specific sport
            List<String> userTags = extractUserTagsSimple(currentUser, sportType);
            logger.info("[FALLBACK_RANKING] User tags: {}", userTags);
            
            // Calculate compatibility score for each match
            for (OpenMatchDto match : matches) {
                double compatibilityScore = calculateMatchCompatibilityScore(userTags, match);
                match.setCompatibilityScore(compatibilityScore);
                logger.info("[FALLBACK_RANKING] Match {} compatibility score: {}", match.getId(), compatibilityScore);
            }
            
            // Sort matches by compatibility score (highest first)
            List<OpenMatchDto> rankedMatches = matches.stream()
                .sorted((m1, m2) -> Double.compare(
                    m2.getCompatibilityScore() != null ? m2.getCompatibilityScore() : 0.0,
                    m1.getCompatibilityScore() != null ? m1.getCompatibilityScore() : 0.0
                ))
                .collect(Collectors.toList());
            
            logger.info("[FALLBACK_RANKING] Successfully ranked {} matches using fallback method", rankedMatches.size());
            return rankedMatches;
            
        } catch (Exception e) {
            logger.error("[FALLBACK_RANKING] Error in fallback ranking: {}", e.getMessage(), e);
            // Return original matches without scores if fallback fails
            return matches;
        }
    }
    
    /**
     * Calculate compatibility score between user tags and match requirements
     */
    private double calculateMatchCompatibilityScore(List<String> userTags, OpenMatchDto match) {
        try {
            List<String> matchTags = match.getRequiredTags();
            if (matchTags == null || matchTags.isEmpty()) {
                // If no specific requirements, give a moderate score
                return 0.6;
            }
            
            if (userTags == null || userTags.isEmpty()) {
                // If user has no tags, give a low score
                return 0.3;
            }
            
            // Calculate tag overlap
            Set<String> userTagsSet = userTags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
            
            Set<String> matchTagsSet = matchTags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
            
            // Find intersection
            Set<String> intersection = new HashSet<>(userTagsSet);
            intersection.retainAll(matchTagsSet);
            
            // Calculate compatibility score
            double overlapRatio = (double) intersection.size() / Math.max(matchTagsSet.size(), 1);
            
            // Apply scoring logic:
            // - High overlap (>= 0.7): 0.8-0.95
            // - Medium overlap (0.3-0.7): 0.5-0.8
            // - Low overlap (< 0.3): 0.2-0.5
            double baseScore;
            if (overlapRatio >= 0.7) {
                baseScore = 0.8 + (overlapRatio - 0.7) * 0.5; // 0.8 to 0.95
            } else if (overlapRatio >= 0.3) {
                baseScore = 0.5 + (overlapRatio - 0.3) * 0.75; // 0.5 to 0.8
            } else {
                baseScore = 0.2 + overlapRatio * 1.0; // 0.2 to 0.5
            }
            
            // Add some randomness to avoid identical scores
            double randomFactor = 0.95 + (Math.random() * 0.1); // 0.95 to 1.05
            double finalScore = Math.min(0.95, baseScore * randomFactor);
            
            logger.debug("[FALLBACK_RANKING] Match {} - User tags: {}, Match tags: {}, Overlap: {}/{}, Score: {}",
                        match.getId(), userTagsSet, matchTagsSet, intersection.size(), matchTagsSet.size(), finalScore);
            
            return finalScore;
            
        } catch (Exception e) {
            logger.error("[FALLBACK_RANKING] Error calculating compatibility for match {}: {}", match.getId(), e.getMessage());
            return 0.5; // Default moderate score
        }
    }
    
    /**
     * Convert single User to Map
     */
    private Map<String, Object> convertUserToMap(User user) {
        try {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("fullName", user.getFullName());
            userData.put("email", user.getEmail());
            userData.put("phoneNumber", user.getPhoneNumber());
            userData.put("address", user.getAddress());
            userData.put("isDiscoverable", user.getIsDiscoverable());
            
            // Parse sport profiles
            if (user.getSportProfiles() != null && !user.getSportProfiles().isEmpty()) {
                Map<String, SportProfileDto> sportProfiles = objectMapper.readValue(
                    user.getSportProfiles(), 
                    new TypeReference<Map<String, SportProfileDto>>() {}
                );
                userData.put("sportProfiles", sportProfiles);
            } else {
                userData.put("sportProfiles", new HashMap<>());
            }
            
            return userData;
        } catch (Exception e) {
            logger.error("Error converting user {} to map: {}", user.getId(), e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * Recommend teammates based on user's sport profile tags with retry logic
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<Map<String, Object>> recommendTeammates(User user, List<User> potentialTeammates, String sportType) {
        try {
            // Validate input data
            validateRecommendationData(user, potentialTeammates, sportType);
            
            // Extract user tags for the specific sport
            List<String> userTags = extractUserTags(user, sportType);
            
            if (userTags.isEmpty()) {
                logger.warn("No tags found for user {} and sport {}, using fallback recommendation", user.getId(), sportType);
                return fallbackRecommendation(potentialTeammates);
            }
            
            // Convert users to the format expected by AI service
            List<Map<String, Object>> teammateData = convertUsersToMaps(potentialTeammates);
            
            // Prepare request payload
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("userTags", userTags);
            requestPayload.put("potentialTeammates", teammateData);
            
            // ===== AUDIT LOGGING: Log the exact payload being sent to AI service =====
            try {
                String payloadJson = objectMapper.writeValueAsString(requestPayload);
                logger.info("[AI_AUDIT] Sending teammate recommendation request to AI service:");
                logger.info("[AI_AUDIT] URL: {}", aiServiceBaseUrl + "/recommend-teammates");
                logger.info("[AI_AUDIT] User ID: {}, Sport: {}", user.getId(), sportType);
                logger.info("[AI_AUDIT] User Tags: {}", userTags);
                logger.info("[AI_AUDIT] Potential Teammates Count: {}", potentialTeammates.size());
                logger.info("[AI_AUDIT] Full Payload JSON: {}", payloadJson);
            } catch (Exception logEx) {
                logger.error("[AI_AUDIT] Failed to serialize payload for logging: {}", logEx.getMessage());
            }
            
            // Call AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestPayload, headers);
            
            String url = aiServiceBaseUrl + "/recommend-teammates";
            logger.info("[AI_AUDIT] Making HTTP POST request to: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            // ===== AUDIT LOGGING: Log the response from AI service =====
            logger.info("[AI_AUDIT] Received response from AI service:");
            logger.info("[AI_AUDIT] Response Status: {}", response.getStatusCode());
            try {
                String responseJson = objectMapper.writeValueAsString(response.getBody());
                logger.info("[AI_AUDIT] Response Body JSON: {}", responseJson);
            } catch (Exception logEx) {
                logger.error("[AI_AUDIT] Failed to serialize response for logging: {}", logEx.getMessage());
                logger.info("[AI_AUDIT] Response Body (raw): {}", response.getBody());
            }
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> recommendedTeammates = (List<Map<String, Object>>) response.getBody().get("recommendedTeammates");
                logger.info("[AI_AUDIT] Successfully extracted {} recommended teammates", recommendedTeammates != null ? recommendedTeammates.size() : 0);
                return recommendedTeammates;
            }
             
            logger.error("[AI_AUDIT] AI service returned non-OK status: {}", response.getStatusCode());
            throw new RuntimeException("AI service returned status: " + response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("[AI_AUDIT] Exception occurred while calling AI service:");
            logger.error("[AI_AUDIT] Exception Type: {}", e.getClass().getSimpleName());
            logger.error("[AI_AUDIT] Exception Message: {}", e.getMessage());
            logger.error("[AI_AUDIT] Full Exception Stack Trace:", e);
            logger.error("[AI_AUDIT] All retry attempts failed for user {} sport {}", user.getId(), sportType);
            
            // Use fallback recommendation instead of throwing exception
            logger.info("[AI_AUDIT] Using fallback recommendation due to AI service failure");
            return fallbackRecommendation(potentialTeammates);
        }
    }
    
    /**
     * Extract user tags - SIMPLE VERSION
     */
    private List<String> extractUserTagsSimple(User user, String sportType) {
        if (user == null || sportType == null) {
            return new ArrayList<>();
        }
        
        try {
            if (user.getSportProfiles() == null || user.getSportProfiles().isEmpty()) {
                logger.info("[RANKING] User {} has no sport profiles, using default tags", user.getId());
                return getDefaultTagsForSport(sportType);
            }
            
            Map<String, SportProfileDto> sportProfiles = objectMapper.readValue(
                user.getSportProfiles(), 
                new TypeReference<Map<String, SportProfileDto>>() {}
            );
            
            SportProfileDto profile = sportProfiles.get(sportType);
            if (profile != null && profile.getTags() != null && !profile.getTags().isEmpty()) {
                logger.info("[RANKING] Found {} tags for user {} sport {}", 
                           profile.getTags().size(), user.getId(), sportType);
                return new ArrayList<>(profile.getTags());
            }
            
            logger.info("[RANKING] No tags for sport {}, using default", sportType);
            return getDefaultTagsForSport(sportType);
            
        } catch (Exception e) {
            logger.error("[RANKING] Error extracting tags for user {}: {}", user.getId(), e.getMessage());
            return getDefaultTagsForSport(sportType);
        }
    }
    
    /**
     * Get default tags for a sport type
     */
    private List<String> getDefaultTagsForSport(String sportType) {
        List<String> defaultTags = new ArrayList<>();
        if ("FOOTBALL".equalsIgnoreCase(sportType)) {
            defaultTags.addAll(Arrays.asList("beginner", "casual", "team-player"));
        } else if ("BASKETBALL".equalsIgnoreCase(sportType)) {
            defaultTags.addAll(Arrays.asList("beginner", "casual", "team-player"));
        } else {
            defaultTags.addAll(Arrays.asList("beginner", "casual"));
        }
        return defaultTags;
    }
    
    /**
     * Convert ranked matches from AI service - SIMPLE VERSION
     */
    private List<OpenMatchDto> convertRankedMatchesSimple(List<Map<String, Object>> rankedMatches) {
        List<OpenMatchDto> result = new ArrayList<>();
        
        for (int i = 0; i < rankedMatches.size(); i++) {
            Map<String, Object> matchData = rankedMatches.get(i);
            try {
                // Tạo DTO từ original match data
                OpenMatchDto dto = new OpenMatchDto();
                
                // Set basic fields
                setBasicFields(dto, matchData);
                
                // Set AI scores - ĐÂY LÀ PHẦN QUAN TRỌNG
                setAIScores(dto, matchData, i + 1);
                
                result.add(dto);
                
            } catch (Exception e) {
                logger.error("[RANKING] Error converting match {}: {}", i + 1, e.getMessage());
            }
        }
        
        logger.info("[RANKING] Converted {} matches successfully", result.size());
        return result;
    }
    
    /**
     * Set basic fields for OpenMatchDto
     */
    private void setBasicFields(OpenMatchDto dto, Map<String, Object> matchData) {
        if (matchData.get("id") instanceof Number) {
            dto.setId(((Number) matchData.get("id")).longValue());
        }
        if (matchData.get("bookingId") instanceof Number) {
            dto.setBookingId(((Number) matchData.get("bookingId")).longValue());
        }
        if (matchData.get("creatorUserId") instanceof Number) {
            dto.setCreatorUserId(((Number) matchData.get("creatorUserId")).longValue());
        }
        if (matchData.get("creatorUserName") != null) {
            dto.setCreatorUserName(matchData.get("creatorUserName").toString());
        }
        if (matchData.get("creatorAvatarUrl") != null) {
            dto.setCreatorAvatarUrl(matchData.get("creatorAvatarUrl").toString());
        }
        if (matchData.get("sportType") != null) {
            dto.setSportType(matchData.get("sportType").toString());
        }
        if (matchData.get("fieldName") != null) {
            dto.setFieldName(matchData.get("fieldName").toString());
        }
        if (matchData.get("locationAddress") != null) {
            dto.setLocationAddress(matchData.get("locationAddress").toString());
        }
        if (matchData.get("locationName") != null) {
            dto.setLocationName(matchData.get("locationName").toString());
        }
        if (matchData.get("slotsNeeded") instanceof Number) {
            dto.setSlotsNeeded(((Number) matchData.get("slotsNeeded")).intValue());
        }
        if (matchData.get("currentParticipants") instanceof Number) {
            dto.setCurrentParticipants(((Number) matchData.get("currentParticipants")).intValue());
        }
        if (matchData.get("status") != null) {
            dto.setStatus(matchData.get("status").toString());
        }
        
        // Handle time fields - convert from String to Instant if needed
        if (matchData.get("startTime") != null) {
            Object startTimeObj = matchData.get("startTime");
            if (startTimeObj instanceof String) {
                try {
                    dto.setStartTime(java.time.Instant.parse((String) startTimeObj));
                } catch (Exception e) {
                    logger.warn("[RANKING] Could not parse startTime: {}", startTimeObj);
                }
            }
        }
        
        if (matchData.get("endTime") != null) {
            Object endTimeObj = matchData.get("endTime");
            if (endTimeObj instanceof String) {
                try {
                    dto.setEndTime(java.time.Instant.parse((String) endTimeObj));
                } catch (Exception e) {
                    logger.warn("[RANKING] Could not parse endTime: {}", endTimeObj);
                }
            }
        }
        
        // Handle requiredTags
        Object requiredTagsObj = matchData.get("requiredTags");
        if (requiredTagsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> tagsList = (List<String>) requiredTagsObj;
            dto.setRequiredTags(tagsList);
        } else if (requiredTagsObj instanceof String) {
            try {
                List<String> tagsList = objectMapper.readValue((String) requiredTagsObj, new TypeReference<List<String>>() {});
                dto.setRequiredTags(tagsList);
            } catch (Exception e) {
                dto.setRequiredTags(new ArrayList<>());
            }
        } else {
            dto.setRequiredTags(new ArrayList<>());
        }
        
        // Handle participantIds
        Object participantIdsObj = matchData.get("participantIds");
        if (participantIdsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Long> participantIds = (List<Long>) participantIdsObj;
            dto.setParticipantIds(participantIds);
        }
        
        // Set currentUserJoinStatus if available
        if (matchData.get("currentUserJoinStatus") != null) {
            dto.setCurrentUserJoinStatus(matchData.get("currentUserJoinStatus").toString());
        }
    }
    
    /**
     * Set AI scores for OpenMatchDto - CRITICAL PART
     */
    private void setAIScores(OpenMatchDto dto, Map<String, Object> matchData, int matchIndex) {
        // Set compatibilityScore
        Object compatibilityScore = matchData.get("compatibilityScore");
        if (compatibilityScore instanceof Number) {
            double score = ((Number) compatibilityScore).doubleValue();
            dto.setCompatibilityScore(score);
            logger.info("[RANKING] Match {} (ID: {}) - compatibilityScore set to: {}", 
                       matchIndex, dto.getId(), score);
        } else {
            logger.warn("[RANKING] Match {} - compatibilityScore not found or invalid: {}", 
                       matchIndex, compatibilityScore);
            dto.setCompatibilityScore(0.0);
        }
        
        // Set explicitScore if available
        Object explicitScore = matchData.get("explicitScore");
        if (explicitScore instanceof Number) {
            dto.setExplicitScore(((Number) explicitScore).doubleValue());
        }
        
        // Set implicitScore if available
        Object implicitScore = matchData.get("implicitScore");
        if (implicitScore instanceof Number) {
            dto.setImplicitScore(((Number) implicitScore).doubleValue());
        }
    }
    
    /**
     * Extract tags from user's sport profile for a specific sport - LEGACY METHOD
     */
    private List<String> extractUserTags(User user, String sportType) {
        try {
            if (user.getSportProfiles() == null || user.getSportProfiles().isEmpty()) {
                logger.info("[AI_SCORE_FIX] User {} has no sport profiles, providing default combined tags for {}", user.getId(), sportType);
                List<String> combinedTags = new ArrayList<>();
                combinedTags.addAll(getDefaultExplicitTags(sportType));
                combinedTags.addAll(getDefaultImplicitTags(sportType));
                return combinedTags;
            }
            
            // Parse sport profiles JSON
            Map<String, SportProfileDto> sportProfiles = objectMapper.readValue(
                user.getSportProfiles(), 
                new TypeReference<Map<String, SportProfileDto>>() {}
            );
            
            SportProfileDto profile = sportProfiles.get(sportType);
            if (profile != null && profile.getTags() != null) {
                logger.info("[AI_SCORE_FIX] User {} has {} tags for {}", user.getId(), profile.getTags().size(), sportType);
                return new ArrayList<>(profile.getTags());
            }
            
            logger.info("[AI_SCORE_FIX] User {} has no profile for {}, providing default combined tags", user.getId(), sportType);
            List<String> combinedTags = new ArrayList<>();
            combinedTags.addAll(getDefaultExplicitTags(sportType));
            combinedTags.addAll(getDefaultImplicitTags(sportType));
            return combinedTags;
            
        } catch (Exception e) {
            logger.error("Error extracting user tags: {}", e.getMessage(), e);
            List<String> combinedTags = new ArrayList<>();
            combinedTags.addAll(getDefaultExplicitTags(sportType));
            combinedTags.addAll(getDefaultImplicitTags(sportType));
            return combinedTags;
        }
    }
    
    /**
     * Convert AI service response back to OpenMatchDto list
     */

    
    /**
     * Convert User entities to maps for AI service
     * AI service expects each teammate to have a 'tags' field with all sport tags combined
     */
    private List<Map<String, Object>> convertUsersToMaps(List<User> users) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (User user : users) {
            try {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("fullName", user.getFullName());
                userData.put("email", user.getEmail());
                userData.put("phoneNumber", user.getPhoneNumber());
                userData.put("address", user.getAddress());
                userData.put("isDiscoverable", user.getIsDiscoverable());
                
                // Extract all tags from all sport profiles for AI service
                List<String> allTags = new ArrayList<>();
                
                if (user.getSportProfiles() != null && !user.getSportProfiles().isEmpty()) {
                    try {
                        Map<String, SportProfileDto> sportProfiles = objectMapper.readValue(
                            user.getSportProfiles(), 
                            new TypeReference<Map<String, SportProfileDto>>() {}
                        );
                        
                        // Collect tags from all sport profiles
                        for (SportProfileDto profile : sportProfiles.values()) {
                            if (profile.getTags() != null) {
                                allTags.addAll(profile.getTags());
                            }
                        }
                        
                        // Keep sportProfiles for backward compatibility
                        userData.put("sportProfiles", sportProfiles);
                    } catch (Exception e) {
                        logger.error("Error parsing sport profiles for user {}: {}", user.getId(), e.getMessage());
                        userData.put("sportProfiles", new HashMap<>());
                    }
                } else {
                    userData.put("sportProfiles", new HashMap<>());
                }
                
                // Add the tags field that AI service expects
                userData.put("tags", allTags);
                
                logger.debug("[AI_AUDIT] Converted user {} with {} tags: {}", user.getId(), allTags.size(), allTags);
                
                result.add(userData);
                
            } catch (Exception e) {
                logger.error("Error converting user {} to map: {}", user.getId(), e.getMessage(), e);
            }
        }
        
        return result;
    }
    
    /**
     * Recommend teammates using hybrid scoring model
     */
    public List<Map<String, Object>> recommendTeammatesHybrid(User user, List<User> potentialTeammates, String sportType) {
        try {
            // Validate input data
            validateRecommendationData(user, potentialTeammates, sportType);
            
            // Prepare hybrid format payload
            Map<String, Object> requestPayload = createHybridPayload(user, potentialTeammates, sportType);
            
            // Log the hybrid payload
            try {
                String payloadJson = objectMapper.writeValueAsString(requestPayload);
                logger.info("[HYBRID_AI_AUDIT] Sending hybrid teammate recommendation request:");
                logger.info("[HYBRID_AI_AUDIT] User ID: {}, Sport: {}", user.getId(), sportType);
                logger.info("[HYBRID_AI_AUDIT] Payload: {}", payloadJson);
            } catch (Exception logEx) {
                logger.error("[HYBRID_AI_AUDIT] Failed to serialize hybrid payload: {}", logEx.getMessage());
            }
            
            // Call AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestPayload, headers);
            
            String url = aiServiceBaseUrl + "/recommend-teammates";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> recommendedTeammates = (List<Map<String, Object>>) response.getBody().get("recommendedTeammates");
                logger.info("[HYBRID_AI_AUDIT] Successfully received {} hybrid recommendations", recommendedTeammates != null ? recommendedTeammates.size() : 0);
                return recommendedTeammates;
            }
            
            logger.error("[HYBRID_AI_AUDIT] AI service returned non-OK status: {}", response.getStatusCode());
            return fallbackRecommendation(potentialTeammates);
            
        } catch (Exception e) {
            logger.error("[HYBRID_AI_AUDIT] Exception in hybrid recommendation: {}", e.getMessage(), e);
            return fallbackRecommendation(potentialTeammates);
        }
    }
    
    /**
     * Rank open matches using hybrid scoring model
     */

    
    /**
     * Create hybrid format payload for teammate recommendations
     */
    private Map<String, Object> createHybridPayload(User user, List<User> potentialTeammates, String sportType) {
        Map<String, Object> payload = new HashMap<>();
        
        // Create current user data
        Map<String, Object> currentUser = createCurrentUserData(user, sportType);
        payload.put("currentUser", currentUser);
        
        // Create candidates data
        List<Map<String, Object>> candidates = new ArrayList<>();
        for (User teammate : potentialTeammates) {
            Map<String, Object> candidate = new HashMap<>();
            candidate.put("userId", teammate.getId());
            candidate.put("explicitTags", extractExplicitTags(teammate, sportType));
            candidate.put("implicitTags", extractImplicitTags(teammate, sportType));
            
            // Add additional user info for compatibility
            candidate.put("fullName", teammate.getFullName());
            candidate.put("email", teammate.getEmail());
            candidate.put("phoneNumber", teammate.getPhoneNumber());
            candidate.put("address", teammate.getAddress());
            candidate.put("isDiscoverable", teammate.getIsDiscoverable());
            
            candidates.add(candidate);
        }
        payload.put("candidates", candidates);
        
        return payload;
    }
    
    /**
     * Create current user data for hybrid format
     */
    private Map<String, Object> createCurrentUserData(User user, String sportType) {
        logger.info("[AI_SCORE_DEBUG] ========== CREATING USER DATA ===========");
        logger.info("[AI_SCORE_DEBUG] User ID: {}, Sport Type: {}", user.getId(), sportType);
        
        Map<String, Object> currentUser = new HashMap<>();
        currentUser.put("userId", user.getId());
        
        // Extract and log explicit tags
        List<String> explicitTags = extractExplicitTags(user, sportType);
        currentUser.put("explicitTags", explicitTags);
        logger.info("[AI_SCORE_DEBUG] Explicit Tags: {}", explicitTags);
        
        // Extract and log implicit tags
        List<String> implicitTags = extractImplicitTags(user, sportType);
        currentUser.put("implicitTags", implicitTags);
        logger.info("[AI_SCORE_DEBUG] Implicit Tags: {}", implicitTags);
        
        // Calculate and log activity level
        int activityLevel = calculateActivityLevel(user);
        currentUser.put("activityLevel", activityLevel);
        logger.info("[AI_SCORE_DEBUG] Activity Level: {}", activityLevel);
        
        // Log complete user data
        try {
            String userDataJson = objectMapper.writeValueAsString(currentUser);
            logger.info("[AI_SCORE_DEBUG] Complete User Data: {}", userDataJson);
        } catch (Exception e) {
            logger.error("[AI_SCORE_DEBUG] Failed to serialize user data: {}", e.getMessage());
        }
        
        logger.info("[AI_SCORE_DEBUG] ========== USER DATA CREATION COMPLETE ===========");
        return currentUser;
    }
    
    /**
     * Extract explicit tags from user's sport profile (user-declared data)
     */
    private List<String> extractExplicitTags(User user, String sportType) {
        try {
            if (user.getSportProfiles() == null || user.getSportProfiles().isEmpty()) {
                logger.info("[AI_SCORE_FIX] User {} has no sport profiles, providing default explicit tags for {}", user.getId(), sportType);
                return getDefaultExplicitTags(sportType);
            }
            
            Map<String, SportProfileDto> sportProfiles = objectMapper.readValue(
                user.getSportProfiles(), 
                new TypeReference<Map<String, SportProfileDto>>() {}
            );
            
            SportProfileDto profile = sportProfiles.get(sportType);
            if (profile != null && profile.getTags() != null) {
                // Filter out implicit tags and keep only explicit ones
                List<String> explicitTags = new ArrayList<>();
                for (String tag : profile.getTags()) {
                    if (isExplicitTag(tag)) {
                        explicitTags.add(tag);
                    }
                }
                logger.info("[AI_SCORE_FIX] User {} has {} explicit tags for {}", user.getId(), explicitTags.size(), sportType);
                return explicitTags.isEmpty() ? getDefaultExplicitTags(sportType) : explicitTags;
            }
            
            logger.info("[AI_SCORE_FIX] User {} has no profile for {}, providing default explicit tags", user.getId(), sportType);
            return getDefaultExplicitTags(sportType);
            
        } catch (Exception e) {
            logger.error("Error extracting explicit tags for user {}: {}", user.getId(), e.getMessage());
            return getDefaultExplicitTags(sportType);
        }
    }
    
    /**
     * Extract implicit tags from user's sport profile (behavioral data)
     */
    private List<String> extractImplicitTags(User user, String sportType) {
        try {
            if (user.getSportProfiles() == null || user.getSportProfiles().isEmpty()) {
                logger.info("[AI_SCORE_FIX] User {} has no sport profiles, providing default implicit tags for {}", user.getId(), sportType);
                return getDefaultImplicitTags(sportType);
            }
            
            Map<String, SportProfileDto> sportProfiles = objectMapper.readValue(
                user.getSportProfiles(), 
                new TypeReference<Map<String, SportProfileDto>>() {}
            );
            
            SportProfileDto profile = sportProfiles.get(sportType);
            if (profile != null && profile.getTags() != null) {
                // Filter out explicit tags and keep only implicit ones
                List<String> implicitTags = new ArrayList<>();
                for (String tag : profile.getTags()) {
                    if (!isExplicitTag(tag)) {
                        implicitTags.add(tag);
                    }
                }
                logger.info("[AI_SCORE_FIX] User {} has {} implicit tags for {}", user.getId(), implicitTags.size(), sportType);
                return implicitTags.isEmpty() ? getDefaultImplicitTags(sportType) : implicitTags;
            }
            
            logger.info("[AI_SCORE_FIX] User {} has no profile for {}, providing default implicit tags", user.getId(), sportType);
            return getDefaultImplicitTags(sportType);
            
        } catch (Exception e) {
            logger.error("Error extracting implicit tags for user {}: {}", user.getId(), e.getMessage());
            return getDefaultImplicitTags(sportType);
        }
    }
    
    /**
     * Determine if a tag is explicit (user-declared) or implicit (behavioral)
     */
    private boolean isExplicitTag(String tag) {
        // Define patterns for explicit tags (user-declared preferences)
        List<String> explicitPatterns = Arrays.asList(
            "tiền vệ", "thủ môn", "hậu vệ", "tiền đạo", // positions
            "kỹ thuật tốt", "tốc độ cao", "thể lực tốt", "kinh nghiệm", // skills
            "chơi đồng đội", "lãnh đạo", "sáng tạo", "phòng ngự" // play styles
        );
        
        // Check if tag matches explicit patterns
        for (String pattern : explicitPatterns) {
            if (tag.toLowerCase().contains(pattern.toLowerCase())) {
                return true;
            }
        }
        
        // If tag contains location, time, or field type info, it's likely implicit
        if (tag.contains("quận") || tag.contains("sân") || tag.contains("tối") || 
            tag.contains("sáng") || tag.contains("chiều") || tag.contains("thứ")) {
            return false;
        }
        
        // Default to explicit for unknown tags
        return true;
    }
    
    /**
     * Calculate user activity level based on booking history
     */
    private int calculateActivityLevel(User user) {
        try {
            // Count total bookings for the user
            int totalBookings = bookingRepository.findByUserId(user.getId()).size();
            
            logger.debug("[ACTIVITY_LEVEL] User {} has {} total bookings", user.getId(), totalBookings);
            
            return totalBookings;
            
        } catch (Exception e) {
            logger.error("Error calculating activity level for user {}: {}", user.getId(), e.getMessage());
            return 0; // Default to 0 if calculation fails
        }
    }
    
    /**
     * Check if AI service is available
     */
    public boolean isAIServiceAvailable() {
        try {
            String url = aiServiceBaseUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("AI service health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Provide default explicit tags when user has no sport profile
     */
    private List<String> getDefaultExplicitTags(String sportType) {
        List<String> defaultTags = new ArrayList<>();
        
        if (sportType == null) {
            return defaultTags;
        }
        
        switch (sportType.toUpperCase()) {
            case "BONG_DA":
            case "FOOTBALL":
                defaultTags.addAll(Arrays.asList(
                    "tiền vệ", "kỹ thuật tốt", "chơi đồng đội", "thể lực tốt"
                ));
                break;
            case "CAU_LONG":
            case "BADMINTON":
                defaultTags.addAll(Arrays.asList(
                    "tốc độ cao", "kỹ thuật tốt", "phản xạ nhanh", "thể lực tốt"
                ));
                break;
            case "TENNIS":
                defaultTags.addAll(Arrays.asList(
                    "kỹ thuật tốt", "tốc độ cao", "thể lực tốt", "tập trung cao"
                ));
                break;
            case "BONG_BAN":
            case "TABLE_TENNIS":
                defaultTags.addAll(Arrays.asList(
                    "phản xạ nhanh", "kỹ thuật tốt", "tập trung cao", "tốc độ cao"
                ));
                break;
            default:
                // Generic sports tags
                defaultTags.addAll(Arrays.asList(
                    "kỹ thuật tốt", "thể lực tốt", "chơi đồng đội", "tốc độ cao"
                ));
                break;
        }
        
        logger.info("[AI_SCORE_FIX] Generated {} default explicit tags for {}: {}", 
                   defaultTags.size(), sportType, defaultTags);
        return defaultTags;
    }
    
    /**
     * Provide default implicit tags when user has no sport profile
     */
    private List<String> getDefaultImplicitTags(String sportType) {
        List<String> defaultTags = new ArrayList<>();
        
        if (sportType == null) {
            return defaultTags;
        }
        
        // Add location-based and time-based implicit tags
        defaultTags.addAll(Arrays.asList(
            "sân trong nhà", "tối thứ 7", "quận 1", "sáng chủ nhật"
        ));
        
        // Add sport-specific implicit tags
        switch (sportType.toUpperCase()) {
            case "BONG_DA":
            case "FOOTBALL":
                defaultTags.addAll(Arrays.asList(
                    "sân cỏ tự nhiên", "11 người", "90 phút"
                ));
                break;
            case "CAU_LONG":
            case "BADMINTON":
                defaultTags.addAll(Arrays.asList(
                    "đơn nam", "60 phút", "sân trong nhà"
                ));
                break;
            case "TENNIS":
                defaultTags.addAll(Arrays.asList(
                    "sân cứng", "đơn nam", "90 phút"
                ));
                break;
            case "BONG_BAN":
            case "TABLE_TENNIS":
                defaultTags.addAll(Arrays.asList(
                    "đơn nam", "45 phút", "sân trong nhà"
                ));
                break;
        }
        
        logger.info("[AI_SCORE_FIX] Generated {} default implicit tags for {}: {}", 
                   defaultTags.size(), sportType, defaultTags);
        return defaultTags;
    }
    
    /**
     * Fallback ranking for draft matches when AI ranking service fails
     * Uses simple tag-based compatibility scoring
     */
    public List<DraftMatchDto> fallbackRankDraftMatches(User currentUser, List<DraftMatchDto> matches, String sportType) {
        try {
            logger.info("[FALLBACK_RANKING] Using fallback ranking for {} draft matches, user: {}, sport: {}", 
                       matches.size(), currentUser.getId(), sportType);
            
            if (matches == null || matches.isEmpty()) {
                return new ArrayList<>();
            }
            
            // Extract user tags for the specific sport
            List<String> userTags = extractUserTagsSimple(currentUser, sportType);
            logger.info("[FALLBACK_RANKING] User tags: {}", userTags);
            
            // Calculate compatibility score for each draft match
            for (DraftMatchDto match : matches) {
                double compatibilityScore = calculateDraftMatchCompatibilityScore(userTags, match);
                match.setCompatibilityScore(compatibilityScore);
                logger.info("[FALLBACK_RANKING] Draft Match {} compatibility score: {}", match.getId(), compatibilityScore);
            }
            
            // Sort matches by compatibility score (highest first)
            List<DraftMatchDto> rankedMatches = matches.stream()
                .sorted((m1, m2) -> Double.compare(
                    m2.getCompatibilityScore() != null ? m2.getCompatibilityScore() : 0.0,
                    m1.getCompatibilityScore() != null ? m1.getCompatibilityScore() : 0.0
                ))
                .collect(Collectors.toList());
            
            logger.info("[FALLBACK_RANKING] Successfully ranked {} draft matches using fallback method", rankedMatches.size());
            return rankedMatches;
            
        } catch (Exception e) {
            logger.error("[FALLBACK_RANKING] Error in fallback ranking for draft matches: {}", e.getMessage(), e);
            // Return original matches without scores if fallback fails
            return matches;
        }
    }
    
    /**
     * Calculate compatibility score between user tags and draft match requirements
     */
    private double calculateDraftMatchCompatibilityScore(List<String> userTags, DraftMatchDto match) {
        try {
            List<String> matchTags = match.getRequiredTags();
            if (matchTags == null || matchTags.isEmpty()) {
                // If no specific requirements, give a moderate score
                return 0.65;
            }
            
            if (userTags == null || userTags.isEmpty()) {
                // If user has no tags, give a low score
                return 0.35;
            }
            
            // Calculate tag overlap
            Set<String> userTagsSet = userTags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
            
            Set<String> matchTagsSet = matchTags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
            
            // Find intersection
            Set<String> intersection = new HashSet<>(userTagsSet);
            intersection.retainAll(matchTagsSet);
            
            // Calculate compatibility score
            double overlapRatio = (double) intersection.size() / Math.max(matchTagsSet.size(), 1);
            
            // Apply scoring logic for draft matches (slightly different from open matches):
            // - High overlap (>= 0.7): 0.75-0.9
            // - Medium overlap (0.3-0.7): 0.45-0.75
            // - Low overlap (< 0.3): 0.25-0.45
            double baseScore;
            if (overlapRatio >= 0.7) {
                baseScore = 0.75 + (overlapRatio - 0.7) * 0.5; // 0.75 to 0.9
            } else if (overlapRatio >= 0.3) {
                baseScore = 0.45 + (overlapRatio - 0.3) * 0.75; // 0.45 to 0.75
            } else {
                baseScore = 0.25 + overlapRatio * 0.67; // 0.25 to 0.45
            }
            
            // Add some randomness to avoid identical scores
            double randomFactor = 0.95 + (Math.random() * 0.1); // 0.95 to 1.05
            double finalScore = Math.min(0.9, baseScore * randomFactor);
            
            logger.debug("[FALLBACK_RANKING] Draft Match {} - User tags: {}, Match tags: {}, Overlap: {}/{}, Score: {}",
                        match.getId(), userTagsSet, matchTagsSet, intersection.size(), matchTagsSet.size(), finalScore);
            
            return finalScore;
            
        } catch (Exception e) {
            logger.error("[FALLBACK_RANKING] Error calculating compatibility for draft match {}: {}", match.getId(), e.getMessage());
            return 0.5; // Default moderate score
        }
    }
    
}