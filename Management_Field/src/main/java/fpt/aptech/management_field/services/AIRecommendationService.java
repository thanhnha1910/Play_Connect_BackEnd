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
     * Rank open matches based on user's sport profile tags
     */
    public List<OpenMatchDto> rankOpenMatches(User user, List<OpenMatchDto> openMatches, String sportType) {
        try {
            // Extract user tags for the specific sport
            List<String> userTags = extractUserTags(user, sportType);
            
            if (userTags.isEmpty()) {
                logger.info("No tags found for user {} and sport {}, returning original order", user.getId(), sportType);
                return openMatches;
            }
            
            // Prepare request payload
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("userTags", userTags);
            requestPayload.put("openMatches", openMatches);
            
            // Call AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestPayload, headers);
            
            String url = aiServiceBaseUrl + "/rank-matches";
            logger.info("[MATCH_RANKING_DEBUG] Calling AI service at: {}", url);
            logger.info("[MATCH_RANKING_DEBUG] Request payload: userTags={}, openMatches count={}", userTags, openMatches.size());
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            logger.info("[MATCH_RANKING_DEBUG] AI service response status: {}", response.getStatusCode());
            logger.info("[MATCH_RANKING_DEBUG] AI service response body: {}", response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> rankedMatches = (List<Map<String, Object>>) response.getBody().get("ranked_matches");
                logger.info("[MATCH_RANKING_DEBUG] Extracted {} ranked matches from AI response", rankedMatches != null ? rankedMatches.size() : 0);
                
                // Log each match's compatibilityScore before conversion
                if (rankedMatches != null) {
                    for (int i = 0; i < rankedMatches.size(); i++) {
                        Map<String, Object> match = rankedMatches.get(i);
                        Object compatibilityScore = match.get("compatibilityScore");
                        Object matchId = match.get("id");
                        logger.info("[MATCH_RANKING_DEBUG] Match[{}] - ID: {}, compatibilityScore: {} (type: {})", 
                                   i, matchId, compatibilityScore, 
                                   compatibilityScore != null ? compatibilityScore.getClass().getSimpleName() : "null");
                    }
                }
                
                List<OpenMatchDto> convertedMatches = convertToOpenMatchDtos(rankedMatches);
                
                // Log each converted DTO's compatibilityScore
                for (int i = 0; i < convertedMatches.size(); i++) {
                    OpenMatchDto dto = convertedMatches.get(i);
                    logger.info("[MATCH_RANKING_DEBUG] ConvertedDTO[{}] - ID: {}, compatibilityScore: {}", 
                               i, dto.getId(), dto.getCompatibilityScore());
                }
                
                return convertedMatches;
            }
            
            logger.warn("AI service returned non-OK status: {}", response.getStatusCode());
            return openMatches;
            
        } catch (Exception e) {
            logger.error("Error calling AI service for match ranking: {}", e.getMessage(), e);
            return openMatches; // Return original list on error
        }
    }
    
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
     * Extract tags from user's sport profile for a specific sport
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
    private List<OpenMatchDto> convertToOpenMatchDtos(List<Map<String, Object>> rankedMatches) {
        List<OpenMatchDto> result = new ArrayList<>();
        
        logger.info("[PHASE2_AUDIT_CONVERSION] Starting conversion of {} matches to DTOs", rankedMatches != null ? rankedMatches.size() : 0);
        
        for (int index = 0; index < rankedMatches.size(); index++) {
            Map<String, Object> matchData = rankedMatches.get(index);
            try {
                logger.info("[PHASE2_AUDIT_CONVERSION] Converting match {}: Raw data keys: {}", index + 1, matchData.keySet());
                logger.info("[PHASE2_AUDIT_CONVERSION] Match {} raw compatibilityScore: {} (type: {})", 
                    index + 1, matchData.get("compatibilityScore"), 
                    matchData.get("compatibilityScore") != null ? matchData.get("compatibilityScore").getClass().getSimpleName() : "null");
                
                OpenMatchDto dto = objectMapper.convertValue(matchData, OpenMatchDto.class);
                
                logger.info("[PHASE2_AUDIT_CONVERSION] Match {} after ObjectMapper conversion: DTO.compatibilityScore = {}", 
                    index + 1, dto.getCompatibilityScore());
                
                // Ensure compatibilityScore is properly set from AI service response
                if (matchData.containsKey("compatibilityScore")) {
                    Object scoreObj = matchData.get("compatibilityScore");
                    if (scoreObj instanceof Number) {
                        double scoreValue = ((Number) scoreObj).doubleValue();
                        dto.setCompatibilityScore(scoreValue);
                        logger.info("[PHASE2_AUDIT_CONVERSION] CRITICAL FIX: Explicitly set compatibilityScore {} for match {} (ID: {})", 
                            scoreValue, index + 1, dto.getId());
                    } else {
                        logger.warn("[PHASE2_AUDIT_CONVERSION] WARNING: compatibilityScore is not a Number for match {}: {} (type: {})", 
                            index + 1, scoreObj, scoreObj != null ? scoreObj.getClass().getSimpleName() : "null");
                    }
                } else {
                    logger.warn("[PHASE2_AUDIT_CONVERSION] WARNING: No compatibilityScore key found in match {} data", index + 1);
                }
                
                // Set explicitScore from AI service response
                if (matchData.containsKey("explicitScore")) {
                    Object scoreObj = matchData.get("explicitScore");
                    if (scoreObj instanceof Number) {
                        double scoreValue = ((Number) scoreObj).doubleValue();
                        dto.setExplicitScore(scoreValue);
                        logger.info("[PHASE2_AUDIT_CONVERSION] Set explicitScore {} for match {} (ID: {})", 
                            scoreValue, index + 1, dto.getId());
                    }
                }
                
                // Set implicitScore from AI service response
                if (matchData.containsKey("implicitScore")) {
                    Object scoreObj = matchData.get("implicitScore");
                    if (scoreObj instanceof Number) {
                        double scoreValue = ((Number) scoreObj).doubleValue();
                        dto.setImplicitScore(scoreValue);
                        logger.info("[PHASE2_AUDIT_CONVERSION] Set implicitScore {} for match {} (ID: {})", 
                            scoreValue, index + 1, dto.getId());
                    }
                }
                
                logger.info("[PHASE2_AUDIT_CONVERSION] Match {} final DTO.compatibilityScore = {}", 
                    index + 1, dto.getCompatibilityScore());
                
                result.add(dto);
            } catch (Exception e) {
                logger.error("[PHASE2_AUDIT_CONVERSION] Error converting match {} data to DTO: {}", index + 1, e.getMessage(), e);
            }
        }
        
        logger.info("[PHASE2_AUDIT_CONVERSION] Conversion completed. Result size: {}", result.size());
        return result;
    }
    
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
    public List<OpenMatchDto> rankOpenMatchesHybrid(User user, List<OpenMatchDto> openMatches, String sportType) {
        try {
            // Extract user data for hybrid format
            Map<String, Object> currentUser = createCurrentUserData(user, sportType);
            
            // Prepare hybrid format payload
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("currentUser", currentUser);
            requestPayload.put("openMatches", openMatches);
            
            // Log the hybrid payload
            try {
                String payloadJson = objectMapper.writeValueAsString(requestPayload);
                logger.info("[HYBRID_AI_AUDIT] Sending hybrid match ranking request:");
                logger.info("[HYBRID_AI_AUDIT] User ID: {}, Sport: {}", user.getId(), sportType);
                logger.info("[HYBRID_AI_AUDIT] Payload: {}", payloadJson);
            } catch (Exception logEx) {
                logger.error("[HYBRID_AI_AUDIT] Failed to serialize hybrid payload: {}", logEx.getMessage());
            }
            
            // Call AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestPayload, headers);
            
            String url = aiServiceBaseUrl + "/api/v1/recommend/matches-ranking";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            // PHASE 2 AUDIT - BREAKPOINT A: Inspect raw JSON response from Python service
            logger.info("[PHASE2_AUDIT_BREAKPOINT_A] Raw response from Python AI service:");
            logger.info("[PHASE2_AUDIT_BREAKPOINT_A] Status Code: {}", response.getStatusCode());
            logger.info("[PHASE2_AUDIT_BREAKPOINT_A] Response Body: {}", response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> rankedMatches = (List<Map<String, Object>>) response.getBody().get("ranked_matches");
                logger.info("[HYBRID_AI_AUDIT] Successfully received {} hybrid ranked matches", rankedMatches != null ? rankedMatches.size() : 0);
                
                // PHASE 2 AUDIT - Inspect rankedMatches from Python before conversion
                if (rankedMatches != null && !rankedMatches.isEmpty()) {
                    logger.info("[PHASE2_AUDIT_BREAKPOINT_A] Inspecting top 3 matches from Python response:");
                    for (int i = 0; i < Math.min(3, rankedMatches.size()); i++) {
                        Map<String, Object> match = rankedMatches.get(i);
                        logger.info("[PHASE2_AUDIT_BREAKPOINT_A] Match {}: ID={}, compatibilityScore={}", 
                            i+1, match.get("id"), match.get("compatibilityScore"));
                    }
                }
                
                List<OpenMatchDto> finalResult = convertToOpenMatchDtos(rankedMatches);
                
                // PHASE 2 AUDIT - BREAKPOINT B: Inspect final OpenMatchDto list before return
                logger.info("[PHASE2_AUDIT_BREAKPOINT_B] Final List<OpenMatchDto> before return:");
                logger.info("[PHASE2_AUDIT_BREAKPOINT_B] Total DTOs: {}", finalResult != null ? finalResult.size() : 0);
                if (finalResult != null && !finalResult.isEmpty()) {
                    logger.info("[PHASE2_AUDIT_BREAKPOINT_B] Inspecting top 3 OpenMatchDto objects:");
                    for (int i = 0; i < Math.min(3, finalResult.size()); i++) {
                        OpenMatchDto dto = finalResult.get(i);
                        logger.info("[PHASE2_AUDIT_BREAKPOINT_B] DTO {}: ID={}, compatibilityScore={}", 
                            i+1, dto.getId(), dto.getCompatibilityScore());
                    }
                }
                
                return finalResult;
            }
            
            logger.error("[HYBRID_AI_AUDIT] AI service returned non-OK status: {}", response.getStatusCode());
            return openMatches;
            
        } catch (Exception e) {
            logger.error("[HYBRID_AI_AUDIT] Exception in hybrid match ranking: {}", e.getMessage(), e);
            return openMatches;
        }
    }
    
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
     * Generic method to enrich any list of matches with AI compatibility scores
     * This method centralizes AI service calls to avoid code duplication
     * 
     * @param matches List of matches (OpenMatchDto or DraftMatchDto)
     * @param currentUser User requesting the matches
     * @param sportType Sport type for AI scoring
     * @param <T> Generic type extending from base match DTO
     * @return Enriched list with AI compatibility scores
     */
    public <T> List<T> enrichMatchesWithAiScores(List<T> matches, User currentUser, String sportType) {
        // Return original list if no matches or user
        if (matches == null || matches.isEmpty() || currentUser == null || sportType == null) {
            logger.info("[AI_ENRICHMENT] Skipping AI enrichment - matches: {}, user: {}, sport: {}", 
                       matches != null ? matches.size() : 0, 
                       currentUser != null ? currentUser.getId() : "null", 
                       sportType);
            return matches;
        }
        
        try {
            // Check if AI service is available
            if (!isAIServiceAvailable()) {
                logger.warn("[AI_ENRICHMENT] AI service not available, returning original matches");
                return matches;
            }
            
            logger.info("[AI_ENRICHMENT] Starting AI enrichment for {} matches, user: {}, sport: {}", 
                       matches.size(), currentUser.getId(), sportType);
            
            // For OpenMatchDto, use existing hybrid ranking
            if (!matches.isEmpty() && matches.get(0) instanceof OpenMatchDto) {
                @SuppressWarnings("unchecked")
                List<OpenMatchDto> openMatches = (List<OpenMatchDto>) matches;
                List<OpenMatchDto> enrichedMatches = rankOpenMatchesHybrid(currentUser, openMatches, sportType);
                
                logger.info("[AI_ENRICHMENT] Successfully enriched {} OpenMatch DTOs", enrichedMatches.size());
                @SuppressWarnings("unchecked")
                List<T> result = (List<T>) enrichedMatches;
                return result;
            }
            
            // For other types (like DraftMatchDto), we can extend this logic later
            // For now, return original matches
            logger.info("[AI_ENRICHMENT] Match type not supported for AI enrichment yet: {}", 
                       matches.get(0).getClass().getSimpleName());
            return matches;
            
        } catch (Exception e) {
            logger.error("[AI_ENRICHMENT] Error during AI enrichment: {}", e.getMessage(), e);
            // Return original matches on error
            return matches;
        }
    }
    
    /**
     * Specialized method for enriching DraftMatchDto with AI scores
     * This method handles the specific requirements for draft matches
     * 
     * @param draftMatches List of DraftMatchDto
     * @param currentUser User requesting the matches
     * @param sportType Sport type for AI scoring
     * @return Enriched list with AI compatibility scores
     */
    public List<fpt.aptech.management_field.payload.dtos.DraftMatchDto> enrichDraftMatchesWithAiScores(
            List<fpt.aptech.management_field.payload.dtos.DraftMatchDto> draftMatches, 
            User currentUser, 
            String sportType) {
        
        // Return original list if no matches or user
        if (draftMatches == null || draftMatches.isEmpty() || currentUser == null || sportType == null) {
            logger.info("[AI_ENRICHMENT_DRAFT] Skipping AI enrichment - matches: {}, user: {}, sport: {}", 
                       draftMatches != null ? draftMatches.size() : 0, 
                       currentUser != null ? currentUser.getId() : "null", 
                       sportType);
            return draftMatches;
        }
        
        try {
            // Check if AI service is available
            if (!isAIServiceAvailable()) {
                logger.warn("[AI_ENRICHMENT_DRAFT] AI service not available, returning original matches");
                return draftMatches;
            }
            
            logger.info("[AI_ENRICHMENT_DRAFT] Starting AI enrichment for {} draft matches, user: {}, sport: {}", 
                       draftMatches.size(), currentUser.getId(), sportType);
            
            // Extract user data for hybrid format
            Map<String, Object> currentUserData = createCurrentUserData(currentUser, sportType);
            
            // Prepare hybrid format payload
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("currentUser", currentUserData);
            requestPayload.put("openMatches", draftMatches); // AI service expects 'openMatches' key
            
            // Log the hybrid payload
            try {
                String payloadJson = objectMapper.writeValueAsString(requestPayload);
                logger.info("[AI_ENRICHMENT_DRAFT] Sending draft match ranking request:");
                logger.info("[AI_ENRICHMENT_DRAFT] User ID: {}, Sport: {}", currentUser.getId(), sportType);
                logger.info("[AI_ENRICHMENT_DRAFT] Payload: {}", payloadJson);
            } catch (Exception logEx) {
                logger.error("[AI_ENRICHMENT_DRAFT] Failed to serialize payload: {}", logEx.getMessage());
            }
            
            // Call AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestPayload, headers);
            
            String url = aiServiceBaseUrl + "/api/v1/recommend/matches-ranking";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> rankedMatches = (List<Map<String, Object>>) response.getBody().get("rankedMatches");
                logger.info("[AI_ENRICHMENT_DRAFT] Successfully received {} ranked draft matches", 
                           rankedMatches != null ? rankedMatches.size() : 0);
                
                if (rankedMatches != null && !rankedMatches.isEmpty()) {
                    // Convert AI response back to DraftMatchDto list
                    List<fpt.aptech.management_field.payload.dtos.DraftMatchDto> enrichedMatches = 
                        convertMapsToDraftMatchDtos(rankedMatches, draftMatches);
                    
                    // TODO: Save AI scores to database
                    // saveDraftMatchAiScoresToDatabase(enrichedMatches);
                    
                    logger.info("[AI_ENRICHMENT_DRAFT] Successfully enriched {} DraftMatch DTOs", enrichedMatches.size());
                    return enrichedMatches;
                }
            }
            
            logger.warn("[AI_ENRICHMENT_DRAFT] AI service returned non-OK status or empty response: {}", 
                       response.getStatusCode());
            return draftMatches;
            
        } catch (Exception e) {
            logger.error("[AI_ENRICHMENT_DRAFT] Error during AI enrichment: {}", e.getMessage(), e);
            // Return original matches on error
            return draftMatches;
        }
    }
    
    /**
     * Helper method to convert AI service response back to DraftMatchDto list
     */
    private List<fpt.aptech.management_field.payload.dtos.DraftMatchDto> convertMapsToDraftMatchDtos(
            List<Map<String, Object>> rankedMatches, 
            List<fpt.aptech.management_field.payload.dtos.DraftMatchDto> originalDtos) {
        
        if (rankedMatches == null) {
            return originalDtos;
        }
        
        List<fpt.aptech.management_field.payload.dtos.DraftMatchDto> result = new ArrayList<>();
        
        for (Map<String, Object> matchData : rankedMatches) {
            try {
                Long matchId = ((Number) matchData.get("id")).longValue();
                
                // Find the original DTO
                fpt.aptech.management_field.payload.dtos.DraftMatchDto originalDto = originalDtos.stream()
                        .filter(dto -> dto.getId().equals(matchId))
                        .findFirst()
                        .orElse(null);
                
                if (originalDto != null) {
                    // Set AI scores from response
                    if (matchData.containsKey("compatibilityScore")) {
                        Object scoreObj = matchData.get("compatibilityScore");
                        if (scoreObj instanceof Number) {
                            double scoreValue = ((Number) scoreObj).doubleValue();
                            originalDto.setCompatibilityScore(scoreValue);
                            logger.debug("[AI_ENRICHMENT_DRAFT] Set compatibilityScore {} for draft match {}", 
                                        scoreValue, matchId);
                        }
                    }
                    
                    if (matchData.containsKey("explicitScore")) {
                        Object scoreObj = matchData.get("explicitScore");
                        if (scoreObj instanceof Number) {
                            double scoreValue = ((Number) scoreObj).doubleValue();
                            originalDto.setExplicitScore(scoreValue);
                            logger.debug("[AI_ENRICHMENT_DRAFT] Set explicitScore {} for draft match {}", 
                                        scoreValue, matchId);
                        }
                    }
                    
                    if (matchData.containsKey("implicitScore")) {
                        Object scoreObj = matchData.get("implicitScore");
                        if (scoreObj instanceof Number) {
                            double scoreValue = ((Number) scoreObj).doubleValue();
                            originalDto.setImplicitScore(scoreValue);
                            logger.debug("[AI_ENRICHMENT_DRAFT] Set implicitScore {} for draft match {}", 
                                        scoreValue, matchId);
                        }
                    }
                    
                    result.add(originalDto);
                } else {
                    logger.warn("[AI_ENRICHMENT_DRAFT] Could not find original DTO for match ID: {}", matchId);
                }
                
            } catch (Exception e) {
                logger.error("[AI_ENRICHMENT_DRAFT] Error processing match data: {}", e.getMessage(), e);
            }
        }
        
        logger.info("[AI_ENRICHMENT_DRAFT] Conversion completed. Result size: {}", result.size());
        return result;
    }
}