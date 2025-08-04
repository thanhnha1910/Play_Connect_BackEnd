package fpt.aptech.management_field.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.aptech.management_field.payload.dtos.DraftMatchDto;
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
public class AIRankingService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIRankingService.class);
    
    @Value("${ai.service.url:http://localhost:5002}")
    private String aiServiceBaseUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public AIRankingService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Rank open matches using AI hybrid scoring
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<OpenMatchDto> rankOpenMatches(User currentUser, List<OpenMatchDto> matches, String sportType) {
        try {
            if (matches == null || matches.isEmpty()) {
                logger.info("[AI_RANKING] No matches to rank for user {}", currentUser.getId());
                return new ArrayList<>();
            }
            
            logger.info("[AI_RANKING] Ranking {} open matches for user {} with sport {}", 
                       matches.size(), currentUser.getId(), sportType);
            
            // Prepare request payload for AI service
            Map<String, Object> requestPayload = createMatchRankingPayload(currentUser, matches, sportType);
            
            // Log the payload for debugging
            try {
                String payloadJson = objectMapper.writeValueAsString(requestPayload);
                logger.info("[AI_RANKING] Sending match ranking request to AI service:");
                logger.info("[AI_RANKING] URL: {}/api/v1/rank/matches", aiServiceBaseUrl);
                logger.info("[AI_RANKING] Payload: {}", payloadJson);
            } catch (Exception logEx) {
                logger.error("[AI_RANKING] Failed to serialize payload for logging: {}", logEx.getMessage());
            }
            
            // Call AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestPayload, headers);
            
            String url = aiServiceBaseUrl + "/api/v1/rank/matches";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            // Log the response
            logger.info("[AI_RANKING] Received response from AI service:");
            logger.info("[AI_RANKING] Response Status: {}", response.getStatusCode());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> rankedMatches = (List<Map<String, Object>>) response.getBody().get("rankedMatches");
                
                if (rankedMatches != null && !rankedMatches.isEmpty()) {
                    logger.info("[AI_RANKING] Successfully received {} ranked matches", rankedMatches.size());
                    return convertRankedMatchesToDto(rankedMatches, matches);
                } else {
                    logger.warn("[AI_RANKING] AI service returned empty ranked matches");
                    return matches; // Return original matches if AI ranking fails
                }
            }
            
            logger.error("[AI_RANKING] AI service returned non-OK status: {}", response.getStatusCode());
            return matches; // Return original matches if AI service fails
            
        } catch (Exception e) {
            logger.error("[AI_RANKING] Exception occurred while ranking matches: {}", e.getMessage(), e);
            return matches; // Return original matches if exception occurs
        }
    }
    
    /**
     * Rank draft matches using AI hybrid scoring
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<DraftMatchDto> rankDraftMatches(User currentUser, List<DraftMatchDto> draftMatches, String sportType) {
        try {
            if (draftMatches == null || draftMatches.isEmpty()) {
                logger.info("[AI_RANKING] No draft matches to rank for user {}", currentUser.getId());
                return new ArrayList<>();
            }
            
            logger.info("[AI_RANKING] Ranking {} draft matches for user {} with sport {}", 
                       draftMatches.size(), currentUser.getId(), sportType);
            
            // Prepare request payload for AI service
            Map<String, Object> requestPayload = createDraftMatchRankingPayload(currentUser, draftMatches, sportType);
            
            // Log the payload for debugging
            try {
                String payloadJson = objectMapper.writeValueAsString(requestPayload);
                logger.info("[AI_RANKING] Sending draft match ranking request to AI service:");
                logger.info("[AI_RANKING] URL: {}/api/v1/rank/draft-matches", aiServiceBaseUrl);
                logger.info("[AI_RANKING] Payload: {}", payloadJson);
            } catch (Exception logEx) {
                logger.error("[AI_RANKING] Failed to serialize payload for logging: {}", logEx.getMessage());
            }
            
            // Call AI service
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestPayload, headers);
            
            String url = aiServiceBaseUrl + "/api/v1/rank/draft-matches";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            // Log the response
            logger.info("[AI_RANKING] Received response from AI service:");
            logger.info("[AI_RANKING] Response Status: {}", response.getStatusCode());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> rankedDraftMatches = (List<Map<String, Object>>) response.getBody().get("rankedDraftMatches");
                
                if (rankedDraftMatches != null && !rankedDraftMatches.isEmpty()) {
                    logger.info("[AI_RANKING] Successfully received {} ranked draft matches", rankedDraftMatches.size());
                    return convertRankedDraftMatchesToDto(rankedDraftMatches, draftMatches);
                } else {
                    logger.warn("[AI_RANKING] AI service returned empty ranked draft matches");
                    return draftMatches; // Return original draft matches if AI ranking fails
                }
            }
            
            logger.error("[AI_RANKING] AI service returned non-OK status: {}", response.getStatusCode());
            return draftMatches; // Return original draft matches if AI service fails
            
        } catch (Exception e) {
            logger.error("[AI_RANKING] Exception occurred while ranking draft matches: {}", e.getMessage(), e);
            return draftMatches; // Return original draft matches if exception occurs
        }
    }
    
    /**
     * Create payload for match ranking request
     */
    private Map<String, Object> createMatchRankingPayload(User currentUser, List<OpenMatchDto> matches, String sportType) {
        Map<String, Object> payload = new HashMap<>();
        
        // Create current user data
        Map<String, Object> currentUserData = createCurrentUserData(currentUser, sportType);
        payload.put("currentUser", currentUserData);
        
        // Create matches data
        List<Map<String, Object>> matchesData = new ArrayList<>();
        for (OpenMatchDto match : matches) {
            Map<String, Object> matchData = new HashMap<>();
            matchData.put("id", match.getId());
            matchData.put("explicitTags", extractMatchExplicitTags(match));
            matchData.put("implicitTags", extractMatchImplicitTags(match));
            matchData.put("sportType", match.getSportType());
            matchData.put("location", match.getLocationAddress());
            matchData.put("difficulty", extractMatchDifficulty(match));
            
            matchesData.add(matchData);
        }
        payload.put("matches", matchesData);
        
        return payload;
    }
    
    /**
     * Create payload for draft match ranking request
     */
    private Map<String, Object> createDraftMatchRankingPayload(User currentUser, List<DraftMatchDto> draftMatches, String sportType) {
        Map<String, Object> payload = new HashMap<>();
        
        // Create current user data
        Map<String, Object> currentUserData = createCurrentUserData(currentUser, sportType);
        payload.put("currentUser", currentUserData);
        
        // Create draft matches data
        List<Map<String, Object>> draftMatchesData = new ArrayList<>();
        for (DraftMatchDto draftMatch : draftMatches) {
            Map<String, Object> draftMatchData = new HashMap<>();
            draftMatchData.put("id", draftMatch.getId());
            draftMatchData.put("explicitTags", extractDraftMatchExplicitTags(draftMatch));
            draftMatchData.put("implicitTags", extractDraftMatchImplicitTags(draftMatch));
            draftMatchData.put("sportType", draftMatch.getSportType());
            draftMatchData.put("location", draftMatch.getLocationDescription());
            draftMatchData.put("difficulty", extractDraftMatchDifficulty(draftMatch));
            
            draftMatchesData.add(draftMatchData);
        }
        payload.put("draftMatches", draftMatchesData);
        
        return payload;
    }
    
    /**
     * Create current user data for AI ranking
     */
    private Map<String, Object> createCurrentUserData(User user, String sportType) {
        Map<String, Object> currentUser = new HashMap<>();
        currentUser.put("userId", user.getId());
        currentUser.put("explicitTags", extractUserExplicitTags(user, sportType));
        currentUser.put("implicitTags", extractUserImplicitTags(user, sportType));
        currentUser.put("activityLevel", calculateUserActivityLevel(user));
        
        return currentUser;
    }
    
    /**
     * Extract explicit tags from user's sport profile
     */
    private List<String> extractUserExplicitTags(User user, String sportType) {
        try {
            if (user.getSportProfiles() == null || user.getSportProfiles().isEmpty()) {
                return getDefaultExplicitTags(sportType);
            }
            
            Map<String, SportProfileDto> sportProfiles = objectMapper.readValue(
                user.getSportProfiles(), 
                new TypeReference<Map<String, SportProfileDto>>() {}
            );
            
            SportProfileDto profile = sportProfiles.get(sportType);
            if (profile != null && profile.getTags() != null) {
                // Filter explicit tags (skill-related, position-related)
                return profile.getTags().stream()
                    .filter(tag -> isExplicitTag(tag))
                    .collect(Collectors.toList());
            }
            
            return getDefaultExplicitTags(sportType);
            
        } catch (Exception e) {
            logger.error("[AI_RANKING] Error extracting explicit tags for user {}: {}", user.getId(), e.getMessage());
            return getDefaultExplicitTags(sportType);
        }
    }
    
    /**
     * Extract implicit tags from user's sport profile and behavior
     */
    private List<String> extractUserImplicitTags(User user, String sportType) {
        try {
            List<String> implicitTags = new ArrayList<>();
            
            // Add location-based implicit tags
            if (user.getAddress() != null && !user.getAddress().isEmpty()) {
                implicitTags.add(user.getAddress());
            }
            
            // Add activity level based implicit tags
            int activityLevel = calculateUserActivityLevel(user);
            if (activityLevel >= 80) {
                implicitTags.add("highly-active");
            } else if (activityLevel >= 50) {
                implicitTags.add("moderately-active");
            } else {
                implicitTags.add("casual-player");
            }
            
            // Add sport-specific implicit tags
            implicitTags.addAll(getDefaultImplicitTags(sportType));
            
            return implicitTags;
            
        } catch (Exception e) {
            logger.error("[AI_RANKING] Error extracting implicit tags for user {}: {}", user.getId(), e.getMessage());
            return getDefaultImplicitTags(sportType);
        }
    }
    
    /**
     * Calculate user activity level (0-100)
     */
    private int calculateUserActivityLevel(User user) {
        // Simple activity level calculation based on available data
        // This can be enhanced with actual booking history, match participation, etc.
        int activityLevel = 50; // Default moderate activity
        
        // Increase activity level if user has sport profiles
        if (user.getSportProfiles() != null && !user.getSportProfiles().isEmpty()) {
            activityLevel += 20;
        }
        
        // Increase activity level if user is discoverable (indicates active participation)
        if (user.getIsDiscoverable() != null && user.getIsDiscoverable()) {
            activityLevel += 15;
        }
        
        // Cap at 100
        return Math.min(activityLevel, 100);
    }
    
    /**
     * Check if a tag is explicit (skill/position related)
     */
    private boolean isExplicitTag(String tag) {
        String lowerTag = tag.toLowerCase();
        return lowerTag.contains("skill") || lowerTag.contains("position") || 
               lowerTag.contains("level") || lowerTag.contains("experience") ||
               lowerTag.contains("beginner") || lowerTag.contains("intermediate") || 
               lowerTag.contains("advanced") || lowerTag.contains("professional");
    }
    
    /**
     * Get default explicit tags for a sport
     */
    private List<String> getDefaultExplicitTags(String sportType) {
        List<String> tags = new ArrayList<>();
        if ("FOOTBALL".equalsIgnoreCase(sportType)) {
            tags.addAll(Arrays.asList("beginner", "team-player", "midfielder"));
        } else if ("BASKETBALL".equalsIgnoreCase(sportType)) {
            tags.addAll(Arrays.asList("beginner", "team-player", "guard"));
        } else {
            tags.addAll(Arrays.asList("beginner", "team-player"));
        }
        return tags;
    }
    
    /**
     * Get default implicit tags for a sport
     */
    private List<String> getDefaultImplicitTags(String sportType) {
        List<String> tags = new ArrayList<>();
        if ("FOOTBALL".equalsIgnoreCase(sportType)) {
            tags.addAll(Arrays.asList("outdoor", "competitive", "social"));
        } else if ("BASKETBALL".equalsIgnoreCase(sportType)) {
            tags.addAll(Arrays.asList("indoor", "fast-paced", "social"));
        } else {
            tags.addAll(Arrays.asList("recreational", "social"));
        }
        return tags;
    }
    
    /**
     * Extract explicit tags from match
     */
    private List<String> extractMatchExplicitTags(OpenMatchDto match) {
        List<String> tags = new ArrayList<>();
        
        // Add required tags as explicit
        if (match.getRequiredTags() != null) {
            tags.addAll(match.getRequiredTags());
        }
        
        // Add sport type
        if (match.getSportType() != null) {
            tags.add(match.getSportType());
        }
        
        return tags;
    }
    
    /**
     * Extract implicit tags from match
     */
    private List<String> extractMatchImplicitTags(OpenMatchDto match) {
        List<String> tags = new ArrayList<>();
        
        // Add location as implicit
        if (match.getLocationAddress() != null) {
            tags.add(match.getLocationAddress());
        }
        
        if (match.getLocationName() != null) {
            tags.add(match.getLocationName());
        }
        
        // Add time-based implicit tags
        if (match.getStartTime() != null) {
            int hour = match.getStartTime().atZone(java.time.ZoneId.systemDefault()).getHour();
            if (hour >= 6 && hour < 12) {
                tags.add("morning");
            } else if (hour >= 12 && hour < 18) {
                tags.add("afternoon");
            } else {
                tags.add("evening");
            }
        }
        
        return tags;
    }
    
    /**
     * Extract difficulty from match
     */
    private String extractMatchDifficulty(OpenMatchDto match) {
        // Extract difficulty from required tags or default to "intermediate"
        if (match.getRequiredTags() != null) {
            for (String tag : match.getRequiredTags()) {
                String lowerTag = tag.toLowerCase();
                if (lowerTag.contains("beginner") || lowerTag.contains("easy")) {
                    return "beginner";
                } else if (lowerTag.contains("advanced") || lowerTag.contains("expert")) {
                    return "advanced";
                } else if (lowerTag.contains("intermediate")) {
                    return "intermediate";
                }
            }
        }
        return "intermediate"; // Default
    }
    
    /**
     * Extract explicit tags from draft match
     */
    private List<String> extractDraftMatchExplicitTags(DraftMatchDto draftMatch) {
        List<String> tags = new ArrayList<>();
        
        // Add required tags as explicit
        if (draftMatch.getRequiredTags() != null) {
            tags.addAll(draftMatch.getRequiredTags());
        }
        
        // Add sport type
        if (draftMatch.getSportType() != null) {
            tags.add(draftMatch.getSportType());
        }
        
        return tags;
    }
    
    /**
     * Extract implicit tags from draft match
     */
    private List<String> extractDraftMatchImplicitTags(DraftMatchDto draftMatch) {
        List<String> tags = new ArrayList<>();
        
        // Add location as implicit
        if (draftMatch.getLocationDescription() != null) {
            tags.add(draftMatch.getLocationDescription());
        }
        
        // Add time-based implicit tags
        if (draftMatch.getEstimatedStartTime() != null) {
            int hour = draftMatch.getEstimatedStartTime().atZone(java.time.ZoneId.systemDefault()).getHour();
            if (hour >= 6 && hour < 12) {
                tags.add("morning");
            } else if (hour >= 12 && hour < 18) {
                tags.add("afternoon");
            } else {
                tags.add("evening");
            }
        }
        
        return tags;
    }
    
    /**
     * Extract difficulty from draft match
     */
    private String extractDraftMatchDifficulty(DraftMatchDto draftMatch) {
        // Extract difficulty from required tags or default to "intermediate"
        if (draftMatch.getRequiredTags() != null) {
            for (String tag : draftMatch.getRequiredTags()) {
                String lowerTag = tag.toLowerCase();
                if (lowerTag.contains("beginner") || lowerTag.contains("easy")) {
                    return "beginner";
                } else if (lowerTag.contains("advanced") || lowerTag.contains("expert")) {
                    return "advanced";
                } else if (lowerTag.contains("intermediate")) {
                    return "intermediate";
                }
            }
        }
        
        // Default difficulty based on estimated time duration
        if (draftMatch.getEstimatedStartTime() != null && draftMatch.getEstimatedEndTime() != null) {
            long duration = java.time.Duration.between(draftMatch.getEstimatedStartTime(), draftMatch.getEstimatedEndTime()).toHours();
            if (duration >= 2) {
                return "advanced";
            } else if (duration >= 1) {
                return "intermediate";
            }
        }
        
        return "intermediate"; // Default
    }
    
    /**
     * Convert ranked matches from AI service back to DTOs
     */
    private List<OpenMatchDto> convertRankedMatchesToDto(List<Map<String, Object>> rankedMatches, List<OpenMatchDto> originalMatches) {
        List<OpenMatchDto> result = new ArrayList<>();
        
        // Create a map for quick lookup of original matches by ID
        Map<Long, OpenMatchDto> originalMatchMap = originalMatches.stream()
            .collect(Collectors.toMap(OpenMatchDto::getId, match -> match));
        
        for (Map<String, Object> rankedMatch : rankedMatches) {
            try {
                Long matchId = null;
                Object idObj = rankedMatch.get("id");
                if (idObj instanceof Number) {
                    matchId = ((Number) idObj).longValue();
                } else if (idObj instanceof String) {
                    matchId = Long.parseLong((String) idObj);
                }
                
                if (matchId != null && originalMatchMap.containsKey(matchId)) {
                    OpenMatchDto originalMatch = originalMatchMap.get(matchId);
                    OpenMatchDto rankedMatchDto = new OpenMatchDto();
                    
                    // Copy all original data
                    copyOpenMatchData(originalMatch, rankedMatchDto);
                    
                    // Set AI scores
                    setAIScores(rankedMatchDto, rankedMatch);
                    
                    result.add(rankedMatchDto);
                    
                    logger.info("[AI_RANKING] Match {} ranked with compatibility score: {}", 
                               matchId, rankedMatchDto.getCompatibilityScore());
                }
                
            } catch (Exception e) {
                logger.error("[AI_RANKING] Error converting ranked match: {}", e.getMessage());
            }
        }
        
        logger.info("[AI_RANKING] Successfully converted {} ranked matches", result.size());
        return result;
    }
    
    /**
     * Convert ranked draft matches from AI service back to DTOs
     */
    private List<DraftMatchDto> convertRankedDraftMatchesToDto(List<Map<String, Object>> rankedDraftMatches, List<DraftMatchDto> originalDraftMatches) {
        List<DraftMatchDto> result = new ArrayList<>();
        
        // Create a map for quick lookup of original draft matches by ID
        Map<Long, DraftMatchDto> originalDraftMatchMap = originalDraftMatches.stream()
            .collect(Collectors.toMap(DraftMatchDto::getId, draftMatch -> draftMatch));
        
        for (Map<String, Object> rankedDraftMatch : rankedDraftMatches) {
            try {
                Long draftMatchId = null;
                Object idObj = rankedDraftMatch.get("id");
                if (idObj instanceof Number) {
                    draftMatchId = ((Number) idObj).longValue();
                } else if (idObj instanceof String) {
                    draftMatchId = Long.parseLong((String) idObj);
                }
                
                if (draftMatchId != null && originalDraftMatchMap.containsKey(draftMatchId)) {
                    DraftMatchDto originalDraftMatch = originalDraftMatchMap.get(draftMatchId);
                    DraftMatchDto rankedDraftMatchDto = new DraftMatchDto();
                    
                    // Copy all original data
                    copyDraftMatchData(originalDraftMatch, rankedDraftMatchDto);
                    
                    // Set AI scores
                    setDraftMatchAIScores(rankedDraftMatchDto, rankedDraftMatch);
                    
                    result.add(rankedDraftMatchDto);
                    
                    logger.info("[AI_RANKING] Draft match {} ranked with compatibility score: {}", 
                               draftMatchId, rankedDraftMatchDto.getCompatibilityScore());
                }
                
            } catch (Exception e) {
                logger.error("[AI_RANKING] Error converting ranked draft match: {}", e.getMessage());
            }
        }
        
        logger.info("[AI_RANKING] Successfully converted {} ranked draft matches", result.size());
        return result;
    }
    
    /**
     * Copy data from original OpenMatchDto to ranked OpenMatchDto
     */
    private void copyOpenMatchData(OpenMatchDto source, OpenMatchDto target) {
        target.setId(source.getId());
        target.setBookingId(source.getBookingId());
        target.setCreatorUserId(source.getCreatorUserId());
        target.setCreatorUserName(source.getCreatorUserName());
        target.setCreatorAvatarUrl(source.getCreatorAvatarUrl());
        target.setSportType(source.getSportType());
        target.setFieldName(source.getFieldName());
        target.setLocationAddress(source.getLocationAddress());
        target.setLocationName(source.getLocationName());
        target.setSlotsNeeded(source.getSlotsNeeded());
        target.setCurrentParticipants(source.getCurrentParticipants());
        target.setStatus(source.getStatus());
        target.setStartTime(source.getStartTime());
        target.setEndTime(source.getEndTime());
        target.setRequiredTags(source.getRequiredTags());
        target.setParticipantIds(source.getParticipantIds());
        target.setCurrentUserJoinStatus(source.getCurrentUserJoinStatus());
    }
    
    /**
     * Copy data from original DraftMatchDto to ranked DraftMatchDto
     */
    private void copyDraftMatchData(DraftMatchDto source, DraftMatchDto target) {
        target.setId(source.getId());
        target.setCreatorUserId(source.getCreatorUserId());
        target.setCreatorUserName(source.getCreatorUserName());
        target.setCreatorAvatarUrl(source.getCreatorAvatarUrl());
        target.setSportType(source.getSportType());
        target.setLocationDescription(source.getLocationDescription());
        target.setEstimatedStartTime(source.getEstimatedStartTime());
        target.setEstimatedEndTime(source.getEstimatedEndTime());
        target.setSlotsNeeded(source.getSlotsNeeded());
        target.setSkillLevel(source.getSkillLevel());
        target.setRequiredTags(source.getRequiredTags());
        target.setStatus(source.getStatus());
        target.setCreatedAt(source.getCreatedAt());
        target.setInterestedUsersCount(source.getInterestedUsersCount());
        target.setInterestedUserIds(source.getInterestedUserIds());
        target.setPendingUsersCount(source.getPendingUsersCount());
        target.setApprovedUsersCount(source.getApprovedUsersCount());
        target.setUserStatuses(source.getUserStatuses());
        target.setCurrentUserInterested(source.getCurrentUserInterested());
        target.setCurrentUserStatus(source.getCurrentUserStatus());
    }
    
    /**
     * Set AI scores for OpenMatchDto
     */
    private void setAIScores(OpenMatchDto dto, Map<String, Object> rankedMatch) {
        // Set compatibility score
        Object compatibilityScore = rankedMatch.get("compatibilityScore");
        if (compatibilityScore instanceof Number) {
            dto.setCompatibilityScore(((Number) compatibilityScore).doubleValue());
        } else {
            dto.setCompatibilityScore(0.0);
        }
        
        // Set explicit score if available
        Object explicitScore = rankedMatch.get("explicitScore");
        if (explicitScore instanceof Number) {
            dto.setExplicitScore(((Number) explicitScore).doubleValue());
        }
        
        // Set implicit score if available
        Object implicitScore = rankedMatch.get("implicitScore");
        if (implicitScore instanceof Number) {
            dto.setImplicitScore(((Number) implicitScore).doubleValue());
        }
    }
    
    /**
     * Set AI scores for DraftMatchDto
     */
    private void setDraftMatchAIScores(DraftMatchDto dto, Map<String, Object> rankedDraftMatch) {
        // Set compatibility score
        Object compatibilityScore = rankedDraftMatch.get("compatibilityScore");
        if (compatibilityScore instanceof Number) {
            dto.setCompatibilityScore(((Number) compatibilityScore).doubleValue());
        } else {
            dto.setCompatibilityScore(0.0);
        }
        
        // Set explicit score if available
        Object explicitScore = rankedDraftMatch.get("explicitScore");
        if (explicitScore instanceof Number) {
            dto.setExplicitScore(((Number) explicitScore).doubleValue());
        }
        
        // Set implicit score if available
        Object implicitScore = rankedDraftMatch.get("implicitScore");
        if (implicitScore instanceof Number) {
            dto.setImplicitScore(((Number) implicitScore).doubleValue());
        }
    }
}