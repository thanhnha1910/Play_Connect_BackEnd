package fpt.aptech.management_field.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.aptech.management_field.payload.dtos.OpenMatchDto;
import fpt.aptech.management_field.payload.dtos.SportProfileDto;
import fpt.aptech.management_field.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIRecommendationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIRecommendationService.class);
    
    @Value("${ai.service.url:http://localhost:5002}")
    private String aiServiceBaseUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
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
                List<Map<String, Object>> rankedMatches = (List<Map<String, Object>>) response.getBody().get("rankedMatches");
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
                return new ArrayList<>();
            }
            
            // Parse sport profiles JSON
            Map<String, SportProfileDto> sportProfiles = objectMapper.readValue(
                user.getSportProfiles(), 
                new TypeReference<Map<String, SportProfileDto>>() {}
            );
            
            SportProfileDto profile = sportProfiles.get(sportType);
            if (profile != null && profile.getTags() != null) {
                return new ArrayList<>(profile.getTags());
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            logger.error("Error extracting user tags: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Convert AI service response back to OpenMatchDto list
     */
    private List<OpenMatchDto> convertToOpenMatchDtos(List<Map<String, Object>> rankedMatches) {
        List<OpenMatchDto> result = new ArrayList<>();
        
        for (Map<String, Object> matchData : rankedMatches) {
            try {
                OpenMatchDto dto = objectMapper.convertValue(matchData, OpenMatchDto.class);
                result.add(dto);
            } catch (Exception e) {
                logger.error("Error converting match data to DTO: {}", e.getMessage(), e);
            }
        }
        
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
}