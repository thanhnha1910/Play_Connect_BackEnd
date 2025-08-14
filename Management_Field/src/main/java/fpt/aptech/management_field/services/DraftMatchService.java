package fpt.aptech.management_field.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.aptech.management_field.models.DraftMatch;
import fpt.aptech.management_field.models.DraftMatchStatus;
import fpt.aptech.management_field.models.DraftMatchUserStatus;
import fpt.aptech.management_field.models.Notification;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.DraftMatchDto;
import fpt.aptech.management_field.payload.dtos.UserStatusDto;
import fpt.aptech.management_field.payload.request.CreateDraftMatchRequest;
import fpt.aptech.management_field.payload.request.UpdateDraftMatchRequest;
import fpt.aptech.management_field.payload.request.InitiateDraftMatchBookingRequest;
import fpt.aptech.management_field.payload.request.CompleteDraftMatchBookingRequest;
import fpt.aptech.management_field.payload.request.RecommendTeammatesRequest;
import fpt.aptech.management_field.repositories.DraftMatchRepository;
import fpt.aptech.management_field.repositories.DraftMatchUserStatusRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DraftMatchService {
    
    private static final Logger log = LoggerFactory.getLogger(DraftMatchService.class);
    
    @Autowired
    private DraftMatchRepository draftMatchRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DraftMatchUserStatusRepository draftMatchUserStatusRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    

    
    @Autowired
    private UnifiedCompatibilityService unifiedCompatibilityService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Transactional
    public DraftMatchDto createDraftMatch(CreateDraftMatchRequest request, Long creatorUserId) {
        log.info("[DEBUG] Creating draft match for user: {}", creatorUserId);
        log.info("[DEBUG] Request data - sportType: {}, locationDescription: {}, startTime: {}, endTime: {}, slotsNeeded: {}, skillLevel: {}, requiredTags: {}", 
                request.getSportType(), request.getLocationDescription(), request.getEstimatedStartTime(), 
                request.getEstimatedEndTime(), request.getSlotsNeeded(), request.getSkillLevel(), request.getRequiredTags());
        
        Optional<User> userOpt = userRepository.findById(creatorUserId);
        if (userOpt.isEmpty()) {
            log.error("[DEBUG] User not found: {}", creatorUserId);
            throw new RuntimeException("User not found");
        }
        log.info("[DEBUG] User found: {}", userOpt.get().getUsername());
        
        DraftMatch draftMatch = new DraftMatch();
        draftMatch.setCreator(userOpt.get());
        draftMatch.setSportType(request.getSportType());
        draftMatch.setLocationDescription(request.getLocationDescription());
        draftMatch.setEstimatedStartTime(request.getEstimatedStartTime());
        draftMatch.setEstimatedEndTime(request.getEstimatedEndTime());
        draftMatch.setSlotsNeeded(request.getSlotsNeeded());
        draftMatch.setSkillLevel(request.getSkillLevel());
        
        log.info("[DEBUG] DraftMatch object created, converting tags...");
        
        // Convert tags list to JSON string
        try {
            log.info("[DEBUG] Required tags: {}", request.getRequiredTags());
            String tagsJson = objectMapper.writeValueAsString(request.getRequiredTags());
            log.info("[DEBUG] Tags JSON: {}", tagsJson);
            draftMatch.setRequiredTags(tagsJson);
        } catch (JsonProcessingException e) {
            log.error("[DEBUG] Error processing required tags: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing required tags", e);
        }
        
        log.info("[DEBUG] Saving draft match to database...");
        try {
            draftMatch = draftMatchRepository.save(draftMatch);
            log.info("[DEBUG] Draft match saved with ID: {}", draftMatch.getId());
        } catch (Exception e) {
            log.error("[DEBUG] Error saving draft match: {}", e.getMessage(), e);
            throw e;
        }
        
        log.info("[DEBUG] Converting to DTO...");
        DraftMatchDto dto = convertToDto(draftMatch, creatorUserId);
        
        log.info("[DEBUG] Sending WebSocket message...");
        try {
            messagingTemplate.convertAndSend("/topic/draft-match/" + draftMatch.getId(), dto);
            log.info("[DEBUG] WebSocket message sent successfully");
        } catch (Exception e) {
            log.error("[DEBUG] Error sending WebSocket message: {}", e.getMessage(), e);
            // Don't throw here, just log the error
        }
        
        log.info("[DEBUG] Draft match creation completed successfully");
        return dto;
    }
    
    @Transactional
    public synchronized DraftMatchDto expressInterest(Long draftMatchId, Long userId) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        
        // Check if draft match is still recruiting
        if (!DraftMatchStatus.RECRUITING.equals(draftMatch.getStatus())) {
            throw new RuntimeException("This draft match is no longer recruiting");
        }
        
        // Check if user is the creator
        if (draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("You cannot express interest in your own draft match");
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        
        // Check if user already has a status for this draft match
        Optional<DraftMatchUserStatus> existingStatus = draftMatchUserStatusRepository.findByDraftMatchIdAndUserId(draftMatchId, userId);
        if (existingStatus.isPresent()) {
            throw new IllegalStateException("Bạn đã bày tỏ quan tâm đến kèo này rồi.");
        }
        
        // Create new pending status
        DraftMatchUserStatus userStatus = new DraftMatchUserStatus();
        userStatus.setDraftMatch(draftMatch);
        userStatus.setUser(user);
        userStatus.setStatus("PENDING");
        draftMatchUserStatusRepository.save(userStatus);
        
        // Check if draft match is now full (based on approved users)
        Long approvedCount = draftMatchUserStatusRepository.countApprovedUsersByDraftMatchId(draftMatchId);
        if (approvedCount >= draftMatch.getSlotsNeeded()) {
            draftMatch.setStatus(DraftMatchStatus.FULL);
            draftMatch = draftMatchRepository.save(draftMatch);
        }
        
        // Send notification to creator
        try {
            createNotificationForDraftMatchInterest(draftMatch, user);
        } catch (Exception e) {
            log.error("Failed to send notification for draft match interest", e);
        }
        
        // Send confirmation notification to interested user
        try {
            createNotificationForUserExpressedInterest(draftMatch, user);
        } catch (Exception e) {
            log.error("Failed to send confirmation notification to interested user", e);
        }
        
        // Send real-time notification via WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/draft-match/" + draftMatchId, 
                convertToDto(draftMatch, userId));
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification for new interest", e);
        }
        
        DraftMatchDto dto = convertToDto(draftMatch, userId);
        
        // Send real-time update to all subscribers
        messagingTemplate.convertAndSend("/topic/draft-match/" + draftMatch.getId(), dto);
        
        return dto;
    }
    
    @Transactional
    public DraftMatchDto withdrawInterest(Long draftMatchId, Long userId) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        
        // Check if user is the creator
        if (draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("You cannot withdraw interest from your own draft match");
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        
        // Check if user has a status for this draft match
        Optional<DraftMatchUserStatus> userStatusOpt = draftMatchUserStatusRepository.findByDraftMatchIdAndUserId(draftMatchId, userId);
        if (userStatusOpt.isEmpty()) {
            throw new RuntimeException("You have not expressed interest in this draft match");
        }
        
        // Remove user status
        draftMatchUserStatusRepository.delete(userStatusOpt.get());
        
        // If draft match was full, change status back to recruiting
        Long approvedCount = draftMatchUserStatusRepository.countApprovedUsersByDraftMatchId(draftMatchId);
        if (DraftMatchStatus.FULL.equals(draftMatch.getStatus()) && approvedCount < draftMatch.getSlotsNeeded()) {
            draftMatch.setStatus(DraftMatchStatus.RECRUITING);
            draftMatch = draftMatchRepository.save(draftMatch);
        }
        
        // Send notification to creator
        try {
            createNotificationForDraftMatchWithdraw(draftMatch, user);
        } catch (Exception e) {
            log.error("Failed to send notification for draft match withdraw", e);
        }
        
        // Send notification to withdrawing user
        try {
            createNotificationForUserWithdraw(draftMatch, user);
        } catch (Exception e) {
            log.error("Failed to send notification to withdrawing user", e);
        }
        
        // Send real-time notification via WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/draft-match/" + draftMatchId, 
                convertToDto(draftMatch, userId));
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification for user withdrawal", e);
        }
        
        DraftMatchDto dto = convertToDto(draftMatch, userId);
        
        // Send real-time update to all subscribers
        messagingTemplate.convertAndSend("/topic/draft-match/" + draftMatch.getId(), dto);
        
        return dto;
    }
    
    public List<DraftMatchDto> getAllActiveDraftMatches() {
        List<DraftMatch> draftMatches = draftMatchRepository.findAllActiveDraftMatches();
        List<DraftMatchDto> matchDtos = draftMatches.stream()
                .map(dm -> convertToDto(dm, null))
                .collect(Collectors.toList());
        
        // No AI enrichment for anonymous users
        log.info("[AI_ENRICHMENT] getAllActiveDraftMatches() called without user context - returning {} matches without AI scores", matchDtos.size());
        return matchDtos;
    }
    
    public List<DraftMatchDto> getAllActiveDraftMatches(Long userId) {
        List<DraftMatch> draftMatches = draftMatchRepository.findAllActiveDraftMatches();
        
        if (userId != null) {
            // Calculate compatibility scores using UnifiedCompatibilityService
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                List<DraftMatchDto> matchDtos = draftMatches.stream()
                        .map(dm -> convertToDto(dm, userId))
                        .collect(Collectors.toList());
                
                // Calculate compatibility scores for each match
                return calculateCompatibilityScores(user, matchDtos, null);
            }
        }
        
        // For unauthenticated users, return matches without compatibility scores
        List<DraftMatchDto> matchDtos = draftMatches.stream()
                .map(dm -> convertToDto(dm, userId))
                .collect(Collectors.toList());
        
        return matchDtos;
    }
    
    public List<DraftMatchDto> getUserDraftMatches(Long userId) {
        List<DraftMatch> draftMatches = draftMatchRepository.findByCreatorId(userId);
        List<DraftMatchDto> matchDtos = draftMatches.stream()
                .map(dm -> convertToDto(dm, userId))
                .collect(Collectors.toList());
        
        // Return user's own draft matches without AI ranking (AI ranking functionality removed)
        return matchDtos;
    }
    
    public List<DraftMatchDto> getDraftMatchesBySport(String sportType) {
        List<DraftMatch> draftMatches = draftMatchRepository.findActiveDraftMatchesBySportType(sportType);
        List<DraftMatchDto> matchDtos = draftMatches.stream()
                .map(dm -> convertToDto(dm, null))
                .collect(Collectors.toList());
        
        // Return draft matches by sport without AI ranking (AI ranking functionality removed)
        return matchDtos;
    }
    
    public List<DraftMatchDto> getDraftMatchesBySport(String sportType, Long userId) {
        List<DraftMatch> draftMatches = draftMatchRepository.findActiveDraftMatchesBySportType(sportType);
        List<DraftMatchDto> matchDtos = draftMatches.stream()
                .map(dm -> convertToDto(dm, userId))
                .collect(Collectors.toList());
        
        // Return draft matches by sport without AI ranking (AI ranking functionality removed)
        return matchDtos;
    }
    
    /**
     * Get public draft matches (no authentication required)
     * Used for unauthenticated users
     */
    public List<DraftMatchDto> getPublicDraftMatches(String sportType) {
        List<DraftMatch> draftMatches;
        
        if (sportType != null && !sportType.trim().isEmpty()) {
            draftMatches = draftMatchRepository.findActiveDraftMatchesBySportType(sportType);
            log.info("[PUBLIC_DRAFT_MATCHES] Retrieved {} public draft matches for sport: {}", draftMatches.size(), sportType);
        } else {
            draftMatches = draftMatchRepository.findAllActiveDraftMatches();
            log.info("[PUBLIC_DRAFT_MATCHES] Retrieved {} public draft matches (all sports)", draftMatches.size());
        }
        
        List<DraftMatchDto> matchDtos = draftMatches.stream()
                .map(dm -> convertToDto(dm, null)) // null userId for public access
                .collect(Collectors.toList());
        
        return matchDtos;
    }
    
    /**
     * Get public draft matches with compatibility scores for authenticated users
     */
    public List<DraftMatchDto> getPublicDraftMatches(String sportType, Long userId) {
        List<DraftMatch> draftMatches;
        
        if (sportType != null && !sportType.trim().isEmpty()) {
            draftMatches = draftMatchRepository.findActiveDraftMatchesBySportType(sportType);
            log.info("[PUBLIC_DRAFT_MATCHES] Retrieved {} public draft matches for sport: {}", draftMatches.size(), sportType);
        } else {
            draftMatches = draftMatchRepository.findAllActiveDraftMatches();
            log.info("[PUBLIC_DRAFT_MATCHES] Retrieved {} public draft matches (all sports)", draftMatches.size());
        }
        
        if (userId != null) {
            // Calculate compatibility scores using UnifiedCompatibilityService
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                List<DraftMatchDto> matchDtos = draftMatches.stream()
                        .map(dm -> convertToDto(dm, userId))
                        .collect(Collectors.toList());
                
                // Calculate compatibility scores for each match
                return calculateCompatibilityScores(user, matchDtos, sportType);
            }
        }
        
        // For unauthenticated users, return matches without compatibility scores
        List<DraftMatchDto> matchDtos = draftMatches.stream()
                .map(dm -> convertToDto(dm, userId))
                .collect(Collectors.toList());
        
        return matchDtos;
    }
    
    private DraftMatchDto convertToDto(DraftMatch draftMatch, Long userId) {
        DraftMatchDto dto = new DraftMatchDto();
        dto.setId(draftMatch.getId());
        dto.setCreatorUserId(draftMatch.getCreator().getId());
        dto.setCreatorUserName(draftMatch.getCreator().getFullName());
        
        // Set creator avatar URL if available
        if (draftMatch.getCreator().getImageUrl() != null) {
            dto.setCreatorAvatarUrl(draftMatch.getCreator().getImageUrl());
        }
        
        dto.setSportType(draftMatch.getSportType());
        dto.setLocationDescription(draftMatch.getLocationDescription());
        dto.setEstimatedStartTime(draftMatch.getEstimatedStartTime());
        dto.setEstimatedEndTime(draftMatch.getEstimatedEndTime());
        dto.setSlotsNeeded(draftMatch.getSlotsNeeded());
        dto.setSkillLevel(draftMatch.getSkillLevel());
        dto.setStatus(draftMatch.getStatus().toString());
        dto.setCreatedAt(draftMatch.getCreatedAt());
        
        // Convert JSON string back to list
        try {
            List<String> tags = objectMapper.readValue(draftMatch.getRequiredTags(), new TypeReference<List<String>>() {});
            dto.setRequiredTags(tags);
        } catch (JsonProcessingException e) {
            dto.setRequiredTags(new ArrayList<>());
        }
        
        // Get user statuses for this draft match
        List<DraftMatchUserStatus> userStatuses = draftMatchUserStatusRepository.findByDraftMatchId(draftMatch.getId());
        
        // Set interested users information (all users with any status)
        dto.setInterestedUsersCount(userStatuses.size());
        List<Long> interestedUserIds = userStatuses.stream()
                .map(status -> status.getUser().getId())
                .collect(Collectors.toList());
        dto.setInterestedUserIds(interestedUserIds);
        
        // Set approval counts
        Long pendingCount = draftMatchUserStatusRepository.countPendingUsersByDraftMatchId(draftMatch.getId());
        Long approvedCount = draftMatchUserStatusRepository.countApprovedUsersByDraftMatchId(draftMatch.getId());
        dto.setPendingUsersCount(pendingCount.intValue());
        dto.setApprovedUsersCount(approvedCount.intValue());
        
        // Set user statuses list
        List<UserStatusDto> userStatusDtos = userStatuses.stream()
                .map(status -> {
                    UserStatusDto userStatusDto = new UserStatusDto();
                    userStatusDto.setUserId(status.getUser().getId());
                    userStatusDto.setFullName(status.getUser().getFullName());
                    userStatusDto.setImageUrl(status.getUser().getImageUrl());
                    userStatusDto.setEmail(status.getUser().getEmail());
                    userStatusDto.setStatus(status.getStatus());
                    userStatusDto.setRequestedAt(status.getCreatedAt());
                    userStatusDto.setUpdatedAt(status.getUpdatedAt());
                    // Don't set placeholder compatibility score - let AI service handle it or leave as null
                    userStatusDto.setCompatibilityScore(null);
                    return userStatusDto;
                })
                .collect(Collectors.toList());
        dto.setUserStatuses(userStatusDtos);
        
        // Set current user interest and status
        if (userId != null) {
            dto.setCurrentUserInterested(interestedUserIds.contains(userId));
            Optional<DraftMatchUserStatus> currentUserStatus = userStatuses.stream()
                    .filter(status -> status.getUser().getId().equals(userId))
                    .findFirst();
            if (currentUserStatus.isPresent()) {
                dto.setCurrentUserStatus(currentUserStatus.get().getStatus());
            } else {
                dto.setCurrentUserStatus(null);
            }
        } else {
            dto.setCurrentUserInterested(false);
            dto.setCurrentUserStatus(null);
        }
        
        return dto;
    }
    
    private void createNotificationForDraftMatchInterest(DraftMatch draftMatch, User interestedUser) {
        Notification notification = new Notification();
        notification.setRecipient(draftMatch.getCreator());
        notification.setTitle("Có người quan tâm đến kèo nháp");
        notification.setContent(String.format("%s đã bày tỏ quan tâm đến kèo nháp %s của bạn tại %s.", 
                interestedUser.getFullName(), 
                draftMatch.getSportType(),
                draftMatch.getLocationDescription()));
        notification.setType("DRAFT_MATCH_INTEREST");
        notification.setRelatedEntityId(draftMatch.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    
    private void createNotificationForUserExpressedInterest(DraftMatch draftMatch, User interestedUser) {
        Notification notification = new Notification();
        notification.setRecipient(interestedUser);
        notification.setTitle("Đã gửi yêu cầu tham gia kèo");
        notification.setContent(String.format("Bạn đã gửi yêu cầu tham gia kèo %s tại %s. Chờ chủ kèo phê duyệt.", 
                draftMatch.getSportType(),
                draftMatch.getLocationDescription()));
        notification.setType("ACTION_SUCCESS");
        notification.setRelatedEntityId(draftMatch.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    
    private void createNotificationForUserAccepted(DraftMatch draftMatch, User acceptedUser) {
        Notification notification = new Notification();
        notification.setRecipient(acceptedUser);
        notification.setTitle("Bạn đã được chấp nhận tham gia kèo");
        notification.setContent(String.format("Chúc mừng! Bạn đã được chấp nhận vào kèo của %s.", 
                draftMatch.getCreator().getFullName()));
        notification.setType("POSITIVE_ALERT");
        notification.setRelatedEntityId(draftMatch.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    
    private void createNotificationForUserRejected(DraftMatch draftMatch, User rejectedUser) {
        Notification notification = new Notification();
        notification.setRecipient(rejectedUser);
        notification.setTitle("Yêu cầu tham gia kèo bị từ chối");
        notification.setContent(String.format("Rất tiếc, yêu cầu tham gia kèo %s tại %s của bạn đã bị từ chối.", 
                draftMatch.getSportType(),
                draftMatch.getLocationDescription()));
        notification.setType("ACTION_ERROR");
        notification.setRelatedEntityId(draftMatch.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    
    private void createNotificationForDraftMatchWithdraw(DraftMatch draftMatch, User withdrawUser) {
        Notification notification = new Notification();
        notification.setRecipient(draftMatch.getCreator());
        notification.setTitle("Có người rút khỏi kèo nháp");
        notification.setContent(String.format("%s đã rút khỏi kèo nháp %s của bạn tại %s.", 
                withdrawUser.getFullName(), 
                draftMatch.getSportType(),
                draftMatch.getLocationDescription()));
        notification.setType("INFO_UPDATE");
        notification.setRelatedEntityId(draftMatch.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    
    @Transactional
    public DraftMatchDto initiateBooking(Long draftMatchId, Long userId) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        
        // Check if user is the creator
        if (!draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can initiate booking for this draft match");
        }
        
        // Check if draft match is in valid status for booking
        if (!"RECRUITING".equals(draftMatch.getStatus()) && !"FULL".equals(draftMatch.getStatus())) {
            throw new RuntimeException("Draft match is not in a valid status for booking initiation");
        }
        
        // Change status to AWAITING_CONFIRMATION to lock the draft match
        draftMatch.setStatus(DraftMatchStatus.AWAITING_CONFIRMATION);
        draftMatch = draftMatchRepository.save(draftMatch);
        
        return convertToDto(draftMatch, userId);
    }
    
    public List<DraftMatchDto> getRankedDraftMatches(Long userId, String sportType) {
        try {
            // Get user for AI ranking
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                // If user not found, return unranked matches
                return sportType != null ? getDraftMatchesBySport(sportType) : getAllActiveDraftMatches();
            }
            
            User user = userOpt.get();
            log.info("[RECOMMENDATION_RANKING] getRankedDraftMatches - userId: {}, sportType: {}", userId, sportType);
            
            // Get basic matches without AI enrichment first
            List<DraftMatch> draftMatches = sportType != null ? 
                draftMatchRepository.findActiveDraftMatchesBySportType(sportType) :
                draftMatchRepository.findAllActiveDraftMatches();
            
            List<DraftMatchDto> matchDtos = draftMatches.stream()
                .map(dm -> convertToDto(dm, userId))
                .collect(Collectors.toList());
            
            log.info("[RECOMMENDATION_RANKING] Found {} draft matches before recommendation ranking", matchDtos.size());
            
            // Use unified compatibility service for draft matches ranking
            List<DraftMatchDto> rankedMatches = unifiedCompatibilityService.calculateDraftMatchCompatibility(user, matchDtos, sportType);
            
            log.info("[RECOMMENDATION_RANKING] Successfully ranked {} draft matches using recommendation service", rankedMatches.size());
            return rankedMatches;
            
        } catch (Exception e) {
            log.error("[RECOMMENDATION_RANKING] Error in getRankedDraftMatches: {}", e.getMessage(), e);
            
            // Fallback: return unranked matches
            try {
                if (sportType != null && !sportType.isEmpty()) {
                    return getDraftMatchesBySport(sportType, userId);
                } else {
                    return getAllActiveDraftMatches(userId);
                }
            } catch (Exception fallbackException) {
                log.error("[RECOMMENDATION_RANKING] Fallback also failed: {}", fallbackException.getMessage());
                return new ArrayList<>();
            }
        }
    }
    

    
    public List<Object> getInterestedUsers(Long draftMatchId, Long userId) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        
        // Check if user is the creator
        if (!draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can view interested users");
        }
        
        return draftMatch.getInterestedUsers().stream()
                .map(user -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("fullName", user.getFullName());
                    userInfo.put("imageUrl", user.getImageUrl());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("compatibilityScore", 85.0); // Placeholder for AI score
                    userInfo.put("status", "INTERESTED"); // Can be INTERESTED, ACCEPTED, REJECTED
                    return userInfo;
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public DraftMatchDto acceptUser(Long draftMatchId, Long userId, Long targetUserId) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        
        // Check if user is the creator
        if (!draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can accept users");
        }
        
        Optional<User> targetUserOpt = userRepository.findById(targetUserId);
        if (targetUserOpt.isEmpty()) {
            throw new RuntimeException("Target user not found");
        }
        
        User targetUser = targetUserOpt.get();
        
        // Check if target user has a pending status
        Optional<DraftMatchUserStatus> userStatusOpt = draftMatchUserStatusRepository.findByDraftMatchIdAndUserId(draftMatchId, targetUserId);
        if (userStatusOpt.isEmpty()) {
            throw new RuntimeException("User has not expressed interest in this draft match");
        }
        
        DraftMatchUserStatus userStatus = userStatusOpt.get();
        if (!"PENDING".equals(userStatus.getStatus())) {
            throw new RuntimeException("User is not in pending status");
        }
        
        // Update status to approved
        userStatus.setStatus("APPROVED");
        draftMatchUserStatusRepository.save(userStatus);
        
        // Check if draft match is now full
        Long approvedCount = draftMatchUserStatusRepository.countApprovedUsersByDraftMatchId(draftMatchId);
        if (approvedCount >= draftMatch.getSlotsNeeded()) {
            draftMatch.setStatus(DraftMatchStatus.FULL);
            draftMatch = draftMatchRepository.save(draftMatch);
            
            // Manual conversion only - removed auto-convert logic
            // Users must manually convert to real match using the convert button
            log.info("Draft match {} is now full with {} approved users. Waiting for manual conversion.", 
                    draftMatchId, approvedCount);
        }
        
        // Send notification to accepted user
        try {
            createNotificationForUserAccepted(draftMatch, targetUser);
        } catch (Exception e) {
            log.error("Failed to send notification for user acceptance", e);
        }
        
        // Send notification to creator about accepting user
        try {
            createNotificationForCreatorAcceptedUser(draftMatch, targetUser);
        } catch (Exception e) {
            log.error("Failed to send notification to creator for user acceptance", e);
        }
        
        // Send real-time notification via WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/draft-match/" + draftMatchId, 
                convertToDto(draftMatch, targetUserId));
            
            // Send user-specific notification
            Map<String, Object> userNotification = new HashMap<>();
            userNotification.put("type", "DRAFT_MATCH_USER_ACCEPTED");
            userNotification.put("draftMatchId", draftMatchId);
            userNotification.put("targetUserId", targetUserId);
            userNotification.put("message", "Bạn đã được chấp nhận tham gia kèo");
            userNotification.put("draftMatch", convertToDto(draftMatch, targetUserId));
            
            messagingTemplate.convertAndSendToUser(targetUserId.toString(), 
                "/queue/draft-matches", userNotification);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification for user acceptance", e);
        }
        
        DraftMatchDto dto = convertToDto(draftMatch, userId);
        
        // Send real-time update to all subscribers
        messagingTemplate.convertAndSend("/topic/draft-match/" + draftMatch.getId(), dto);
        
        return dto;
    }
    
    @Transactional
    public DraftMatchDto rejectUser(Long draftMatchId, Long userId, Long targetUserId) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        
        // Check if user is the creator
        if (!draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can reject users");
        }
        
        Optional<User> targetUserOpt = userRepository.findById(targetUserId);
        if (targetUserOpt.isEmpty()) {
            throw new RuntimeException("Target user not found");
        }
        
        User targetUser = targetUserOpt.get();
        
        // Check if target user has a pending status
        Optional<DraftMatchUserStatus> userStatusOpt = draftMatchUserStatusRepository.findByDraftMatchIdAndUserId(draftMatchId, targetUserId);
        if (userStatusOpt.isEmpty()) {
            throw new RuntimeException("User has not expressed interest in this draft match");
        }
        
        DraftMatchUserStatus userStatus = userStatusOpt.get();
        if (!"PENDING".equals(userStatus.getStatus())) {
            throw new RuntimeException("User is not in pending status");
        }
        
        // Update status to rejected
        userStatus.setStatus("REJECTED");
        draftMatchUserStatusRepository.save(userStatus);
        
        // Send notification to rejected user
        try {
            createNotificationForUserRejected(draftMatch, targetUser);
        } catch (Exception e) {
            log.error("Failed to send notification for user rejection", e);
        }
        
        // Send notification to creator about rejecting user
        try {
            createNotificationForCreatorRejectedUser(draftMatch, targetUser);
        } catch (Exception e) {
            log.error("Failed to send notification to creator for user rejection", e);
        }
        
        // Send real-time notification via WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/draft-match/" + draftMatchId, 
                convertToDto(draftMatch, targetUserId));
            
            // Send user-specific notification
            Map<String, Object> userNotification = new HashMap<>();
            userNotification.put("type", "DRAFT_MATCH_USER_REJECTED");
            userNotification.put("draftMatchId", draftMatchId);
            userNotification.put("targetUserId", targetUserId);
            userNotification.put("message", "Yêu cầu tham gia kèo của bạn đã bị từ chối");
            userNotification.put("draftMatch", convertToDto(draftMatch, targetUserId));
            
            messagingTemplate.convertAndSendToUser(targetUserId.toString(), 
                "/queue/draft-matches", userNotification);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification for user rejection", e);
        }
        
        DraftMatchDto dto = convertToDto(draftMatch, userId);
        
        // Send real-time update to all subscribers
        messagingTemplate.convertAndSend("/topic/draft-match/" + draftMatch.getId(), dto);
        
        return dto;
    }
    
    @Transactional
    public Object convertToMatch(Long draftMatchId, Long userId) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        
        // Check if user is the creator
        if (!draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can convert draft match to real match");
        }
        
        // Check if draft match status is valid for conversion
        if (!"FULL".equals(draftMatch.getStatus())) {
            throw new RuntimeException("Draft match must be FULL before converting to real match");
        }
        
        // Check if there are enough interested users
        if (draftMatch.getInterestedUsers().size() < draftMatch.getSlotsNeeded()) {
            throw new RuntimeException("Not enough interested users to convert to real match");
        }
        
        // Change status to CONVERTED
        draftMatch.setStatus(DraftMatchStatus.CONVERTED);
        draftMatch = draftMatchRepository.save(draftMatch);
        
        // Create response with booking information
        Map<String, Object> result = new HashMap<>();
        result.put("draftMatchId", draftMatch.getId());
        result.put("status", "CONVERTED");
        result.put("message", "Draft match converted successfully. Proceed to field booking.");
        result.put("participantCount", draftMatch.getInterestedUsers().size() + 1); // +1 for creator
        
        // Send notifications to all interested users
        for (User user : draftMatch.getInterestedUsers()) {
            try {
                createNotificationForMatchConverted(draftMatch, user);
            } catch (Exception e) {
                log.error("Failed to send notification for match conversion", e);
            }
        }
        
        // Send real-time update
        messagingTemplate.convertAndSend("/topic/draft-match/" + draftMatch.getId(), convertToDto(draftMatch, userId));
        
        return result;
    }
    

    
    private void createNotificationForMatchConverted(DraftMatch draftMatch, User participant) {
        Notification notification = new Notification();
        notification.setRecipient(participant);
        notification.setTitle("Kèo nháp đã được chốt");
        notification.setContent(String.format("Kèo nháp %s tại %s đã được chốt thành kèo thật. Chuẩn bị tham gia nhé!", 
                draftMatch.getSportType(),
                draftMatch.getLocationDescription()));
        notification.setType("DRAFT_MATCH_CONVERTED");
        notification.setRelatedEntityId(draftMatch.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    
    @Transactional
    public DraftMatchDto updateDraftMatch(Long draftMatchId, UpdateDraftMatchRequest request, Long userId) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        
        // Check if user is the creator
        if (!draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can update this draft match");
        }
        
        // Check if draft match can still be updated (not converted)
        if ("CONVERTED".equals(draftMatch.getStatus())) {
            throw new RuntimeException("Cannot update a converted draft match");
        }
        
        // Store old values for comparison
        String oldLocation = draftMatch.getLocationDescription();
        LocalDateTime oldStartTime = draftMatch.getEstimatedStartTime();
        LocalDateTime oldEndTime = draftMatch.getEstimatedEndTime();
        
        // Update fields
        if (request.getSportType() != null) {
            draftMatch.setSportType(request.getSportType());
        }
        if (request.getLocation() != null) {
            draftMatch.setLocationDescription(request.getLocation());
        }
        if (request.getPreferredDateTime() != null) {
            // Parse preferred date time if needed
            // For now, we'll skip time updates as the request doesn't have specific time fields
        }
        if (request.getSlotsNeeded() != null) {
            draftMatch.setSlotsNeeded(request.getSlotsNeeded());
        }
        if (request.getSkillLevel() != null) {
            draftMatch.setSkillLevel(request.getSkillLevel());
        }
        // Note: title, description, maxPlayers, notes fields are not available in DraftMatch model
        // These fields from UpdateDraftMatchRequest are ignored for now
        
        draftMatch = draftMatchRepository.save(draftMatch);
        
        // Check if location changed to send notifications
        boolean locationChanged = !Objects.equals(oldLocation, draftMatch.getLocationDescription());
        boolean timeChanged = false; // Time change detection disabled for now
        
        if (locationChanged || timeChanged) {
            // Send notifications to all interested users
            for (User user : draftMatch.getInterestedUsers()) {
                try {
                    createNotificationForDraftMatchUpdate(draftMatch, user, locationChanged, timeChanged);
                } catch (Exception e) {
                    log.error("Failed to send notification for draft match update", e);
                }
            }
            
            // Send WebSocket notification for draft match update
            Map<String, Object> updatePayload = new HashMap<>();
            updatePayload.put("draftMatchId", draftMatch.getId());
            updatePayload.put("sportType", draftMatch.getSportType());
            updatePayload.put("locationDescription", draftMatch.getLocationDescription());
            updatePayload.put("locationChanged", locationChanged);
            updatePayload.put("timeChanged", timeChanged);
            updatePayload.put("interestedUserIds", draftMatch.getInterestedUsers().stream()
                    .map(User::getId)
                    .collect(java.util.stream.Collectors.toList()));
            
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", "DRAFT_MATCH_UPDATED");
            wsMessage.put("payload", updatePayload);
            
            messagingTemplate.convertAndSend("/topic/draft-matches", wsMessage);
        }
        
        DraftMatchDto dto = convertToDto(draftMatch, userId);
        
        // Send real-time update to all subscribers
        messagingTemplate.convertAndSend("/topic/draft-match/" + draftMatch.getId(), dto);
        
        return dto;
    }
    
    private void createNotificationForDraftMatchUpdate(DraftMatch draftMatch, User interestedUser, boolean locationChanged, boolean timeChanged) {
        Notification notification = new Notification();
        notification.setRecipient(interestedUser);
        notification.setTitle("Kèo nháp đã được cập nhật");
        
        String changeDetails = "";
        if (locationChanged && timeChanged) {
            changeDetails = "địa điểm và thời gian";
        } else if (locationChanged) {
            changeDetails = "địa điểm";
        } else if (timeChanged) {
            changeDetails = "thời gian";
        }
        
        notification.setContent(String.format("Chủ kèo đã thay đổi %s của kèo nháp %s. Địa điểm mới: %s. Kiểm tra lại thông tin nhé!", 
                changeDetails,
                draftMatch.getSportType(),
                draftMatch.getLocationDescription()));
        notification.setType("DRAFT_MATCH_UPDATED");
        notification.setRelatedEntityId(draftMatch.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    
    public List<Object> getReceivedDraftMatchRequests(Long userId) {
        // Get all draft matches created by this user
        List<DraftMatch> userDraftMatches = draftMatchRepository.findByCreatorId(userId);
        
        List<Object> requests = new ArrayList<>();
        
        for (DraftMatch draftMatch : userDraftMatches) {
            // Get all pending requests for this draft match
            List<DraftMatchUserStatus> pendingRequests = draftMatchUserStatusRepository
                    .findByDraftMatchIdAndStatus(draftMatch.getId(), "PENDING");
            
            for (DraftMatchUserStatus userStatus : pendingRequests) {
                Map<String, Object> request = new HashMap<>();
                request.put("id", userStatus.getId());
                request.put("type", "DRAFT_MATCH_REQUEST");
                request.put("status", userStatus.getStatus());
                request.put("createdAt", userStatus.getCreatedAt());
                request.put("draftMatchId", draftMatch.getId());
                request.put("userId", userStatus.getUser().getId());
                
                // Draft match details
                Map<String, Object> draftMatchDetails = new HashMap<>();
                draftMatchDetails.put("id", draftMatch.getId());
                draftMatchDetails.put("sportType", draftMatch.getSportType());
                draftMatchDetails.put("locationDescription", draftMatch.getLocationDescription());
                draftMatchDetails.put("estimatedStartTime", draftMatch.getEstimatedStartTime());
                draftMatchDetails.put("estimatedEndTime", draftMatch.getEstimatedEndTime());
                draftMatchDetails.put("slotsNeeded", draftMatch.getSlotsNeeded());
                draftMatchDetails.put("skillLevel", draftMatch.getSkillLevel());
                request.put("draftMatch", draftMatchDetails);
                
                // User details (requester)
                Map<String, Object> userDetails = new HashMap<>();
                userDetails.put("id", userStatus.getUser().getId());
                userDetails.put("username", userStatus.getUser().getUsername());
                userDetails.put("fullName", userStatus.getUser().getFullName());
                userDetails.put("avatarUrl", userStatus.getUser().getImageUrl());
                request.put("user", userDetails);
                
                // Creator details (current user)
                Map<String, Object> creatorDetails = new HashMap<>();
                creatorDetails.put("id", draftMatch.getCreator().getId());
                creatorDetails.put("username", draftMatch.getCreator().getUsername());
                creatorDetails.put("fullName", draftMatch.getCreator().getFullName());
                creatorDetails.put("avatarUrl", draftMatch.getCreator().getImageUrl());
                request.put("creator", creatorDetails);
                
                requests.add(request);
            }
        }
        
        return requests;
    }
    
    public List<Object> getSentDraftMatchRequests(Long userId) {
        // Get all draft match requests sent by this user
        List<DraftMatchUserStatus> userStatuses = draftMatchUserStatusRepository.findByUserId(userId);
        
        List<Object> requests = new ArrayList<>();
        
        for (DraftMatchUserStatus userStatus : userStatuses) {
            DraftMatch draftMatch = userStatus.getDraftMatch();
            
            Map<String, Object> request = new HashMap<>();
            request.put("id", userStatus.getId());
            request.put("type", "DRAFT_MATCH_REQUEST");
            request.put("status", userStatus.getStatus());
            request.put("createdAt", userStatus.getCreatedAt());
            request.put("draftMatchId", draftMatch.getId());
            request.put("userId", userStatus.getUser().getId());
            
            // Draft match details
            Map<String, Object> draftMatchDetails = new HashMap<>();
            draftMatchDetails.put("id", draftMatch.getId());
            draftMatchDetails.put("sportType", draftMatch.getSportType());
            draftMatchDetails.put("locationDescription", draftMatch.getLocationDescription());
            draftMatchDetails.put("estimatedStartTime", draftMatch.getEstimatedStartTime());
            draftMatchDetails.put("estimatedEndTime", draftMatch.getEstimatedEndTime());
            draftMatchDetails.put("slotsNeeded", draftMatch.getSlotsNeeded());
            draftMatchDetails.put("skillLevel", draftMatch.getSkillLevel());
            request.put("draftMatch", draftMatchDetails);
            
            // User details (current user - requester)
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("id", userStatus.getUser().getId());
            userDetails.put("username", userStatus.getUser().getUsername());
            userDetails.put("fullName", userStatus.getUser().getFullName());
            userDetails.put("avatarUrl", userStatus.getUser().getImageUrl());
            request.put("user", userDetails);
            
            // Creator details
            Map<String, Object> creatorDetails = new HashMap<>();
            creatorDetails.put("id", draftMatch.getCreator().getId());
            creatorDetails.put("username", draftMatch.getCreator().getUsername());
            creatorDetails.put("fullName", draftMatch.getCreator().getFullName());
            creatorDetails.put("avatarUrl", draftMatch.getCreator().getImageUrl());
            request.put("creator", creatorDetails);
            
            requests.add(request);
        }
        
        return requests;
    }
    
    private void createNotificationForCreatorAcceptedUser(DraftMatch draftMatch, User acceptedUser) {
        Notification notification = new Notification();
        notification.setRecipient(draftMatch.getCreator());
        notification.setTitle("Đã chấp nhận người chơi");
        notification.setContent(String.format("Bạn đã chấp nhận %s vào kèo.", 
                acceptedUser.getFullName() != null ? acceptedUser.getFullName() : acceptedUser.getUsername()));
        notification.setType("ACTION_SUCCESS");
        notification.setRelatedEntityId(draftMatch.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    
    private void createNotificationForCreatorRejectedUser(DraftMatch draftMatch, User rejectedUser) {
        Notification notification = new Notification();
        notification.setRecipient(draftMatch.getCreator());
        notification.setTitle("Đã từ chối người chơi");
        notification.setContent(String.format("Bạn đã từ chối %s tham gia kèo nháp %s tại %s.", 
                rejectedUser.getFullName() != null ? rejectedUser.getFullName() : rejectedUser.getUsername(),
                draftMatch.getSportType(),
                draftMatch.getLocationDescription()));
        notification.setType("ACTION_SUCCESS");
        notification.setRelatedEntityId(draftMatch.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    
    private void createNotificationForUserWithdraw(DraftMatch draftMatch, User withdrawUser) {
        Notification notification = new Notification();
        notification.setRecipient(withdrawUser);
        notification.setTitle("Rút khỏi kèo nháp thành công");
        notification.setContent(String.format("Bạn đã rút khỏi kèo nháp %s tại %s thành công.", 
                draftMatch.getSportType(),
                draftMatch.getLocationDescription()));
        notification.setType("ACTION_SUCCESS");
        notification.setRelatedEntityId(draftMatch.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    
    public List<Map<String, Object>> getInterestedUsersWithCompatibility(Long draftMatchId, Long userId) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        
        // Check if user is the creator
        if (!draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can view interested users");
        }
        
        List<DraftMatchUserStatus> userStatuses = draftMatchUserStatusRepository.findByDraftMatchId(draftMatchId);
        
        return userStatuses.stream()
                .map(status -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", status.getUser().getId());
                    userInfo.put("fullName", status.getUser().getFullName());
                    userInfo.put("imageUrl", status.getUser().getImageUrl());
                    userInfo.put("email", status.getUser().getEmail());
                    userInfo.put("status", status.getStatus());
                    userInfo.put("requestedAt", status.getCreatedAt());
                    userInfo.put("compatibilityScore", 85.0); // Placeholder for AI score
                    return userInfo;
                })
                .collect(Collectors.toList());
    }
    
    public DraftMatchDto getDraftMatchById(Long draftMatchId, Long userId) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        return convertToDto(draftMatch, userId);
    }
    
    @Transactional
    public boolean cancelDraftMatch(Long draftMatchId, Long userId) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        
        // Check if user is the creator
        if (!draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can cancel this draft match");
        }
        
        // Check if draft match can be cancelled
        if ("CONVERTED".equals(draftMatch.getStatus())) {
            throw new RuntimeException("Cannot cancel a converted draft match");
        }
        
        // Update status to cancelled
        draftMatch.setStatus(DraftMatchStatus.CANCELLED);
        draftMatchRepository.save(draftMatch);
        
        // Send notifications to interested users
        List<DraftMatchUserStatus> userStatuses = draftMatchUserStatusRepository.findByDraftMatchId(draftMatchId);
        for (DraftMatchUserStatus userStatus : userStatuses) {
            try {
                createNotificationForDraftMatchCancelled(draftMatch, userStatus.getUser());
            } catch (Exception e) {
                log.error("Failed to send cancellation notification", e);
            }
        }
        
        return true;
    }
    
    public List<DraftMatchDto> getMyDraftMatches(Long userId) {
        List<DraftMatch> draftMatches = draftMatchRepository.findByCreatorId(userId);
        return draftMatches.stream()
                .map(dm -> convertToDto(dm, userId))
                .collect(Collectors.toList());
    }
    
    public List<DraftMatchDto> getMyInterests(Long userId) {
        List<DraftMatchUserStatus> userStatuses = draftMatchUserStatusRepository.findByUserId(userId);
        return userStatuses.stream()
                .map(status -> convertToDto(status.getDraftMatch(), userId))
                .collect(Collectors.toList());
    }
    
    private void createNotificationForDraftMatchCancelled(DraftMatch draftMatch, User user) {
        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setTitle("Kèo nháp đã bị hủy");
        notification.setContent(String.format("Kèo nháp %s tại %s đã bị hủy bởi chủ kèo.", 
                draftMatch.getSportType(),
                draftMatch.getLocationDescription()));
        notification.setType("ACTION_ERROR");
        notification.setRelatedEntityId(draftMatch.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    
    @Transactional
    public DraftMatchDto removeApprovedUser(Long draftMatchId, Long userId, Long targetUserId) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        
        // Check if user is the creator
        if (!draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can remove approved users");
        }
        
        // Find the user status
        Optional<DraftMatchUserStatus> userStatusOpt = draftMatchUserStatusRepository
                .findByDraftMatchIdAndUserId(draftMatchId, targetUserId);
        
        if (userStatusOpt.isEmpty()) {
            throw new RuntimeException("User not found in this draft match");
        }
        
        DraftMatchUserStatus userStatus = userStatusOpt.get();
        
        // Check if user is approved
        if (!"APPROVED".equals(userStatus.getStatus())) {
            throw new RuntimeException("User is not approved");
        }
        
        // Remove the user status
        draftMatchUserStatusRepository.delete(userStatus);
        
        // Update draft match status if it was full
        if (DraftMatchStatus.FULL.equals(draftMatch.getStatus())) {
            draftMatch.setStatus(DraftMatchStatus.RECRUITING);
            draftMatch = draftMatchRepository.save(draftMatch);
        }
        
        // Send notification to removed user
        Optional<User> removedUserOpt = userRepository.findById(targetUserId);
        if (removedUserOpt.isPresent()) {
            createNotificationForRemovedUser(draftMatch, removedUserOpt.get());
        }
        
        return convertToDto(draftMatch, userId);
    }
    
    @Transactional
    public Map<String, Object> initiateDraftMatchBooking(Long draftMatchId, Long userId, 
            InitiateDraftMatchBookingRequest request) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        
        // Check if user is the creator
        if (!draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can initiate booking");
        }
        
        // Check if draft match has enough approved users
        Long approvedCount = draftMatchUserStatusRepository.countApprovedUsersByDraftMatchId(draftMatchId);
        if (approvedCount < draftMatch.getSlotsNeeded()) {
            throw new RuntimeException("Not enough approved users to initiate booking");
        }
        
        // Update draft match status
        draftMatch.setStatus(DraftMatchStatus.AWAITING_CONFIRMATION);
        draftMatch = draftMatchRepository.save(draftMatch);
        
        // Create booking response
        Map<String, Object> response = new HashMap<>();
        response.put("draftMatchId", draftMatchId);
        response.put("fieldId", request.getFieldId());
        response.put("startTime", request.getStartTime());
        response.put("endTime", request.getEndTime());
        response.put("paymentMethod", request.getPaymentMethod());
        response.put("status", "BOOKING_INITIATED");
        response.put("message", "Booking initiated successfully");
        
        return response;
    }
    
    @Transactional
    public Map<String, Object> completeDraftMatchBooking(Long draftMatchId, Long userId, 
            CompleteDraftMatchBookingRequest request) {
        Optional<DraftMatch> draftMatchOpt = draftMatchRepository.findById(draftMatchId);
        if (draftMatchOpt.isEmpty()) {
            throw new RuntimeException("Draft match not found");
        }
        
        DraftMatch draftMatch = draftMatchOpt.get();
        
        // Check if user is the creator
        if (!draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can complete booking");
        }
        
        // Check if booking was initiated
        if (!"BOOKING_INITIATED".equals(draftMatch.getStatus())) {
            throw new RuntimeException("Booking was not initiated for this draft match");
        }
        
        // Update draft match status
        draftMatch.setStatus(DraftMatchStatus.CONVERTED);
        draftMatch = draftMatchRepository.save(draftMatch);
        
        // Send notifications to all approved users
        List<DraftMatchUserStatus> approvedUsers = draftMatchUserStatusRepository
                .findApprovedUsersByDraftMatchId(draftMatchId);
        
        for (DraftMatchUserStatus userStatus : approvedUsers) {
            createNotificationForBookingCompleted(draftMatch, userStatus.getUser());
        }
        
        // Create completion response
        Map<String, Object> response = new HashMap<>();
        response.put("draftMatchId", draftMatchId);
        response.put("bookingId", request.getBookingId());
        response.put("totalAmount", request.getTotalAmount());
        response.put("paymentStatus", request.getPaymentStatus());
        response.put("transactionId", request.getTransactionId());
        response.put("status", "CONVERTED");
        response.put("message", "Booking completed successfully");
        
        return response;
    }
    
    public List<Map<String, Object>> recommendTeammates(Long userId, RecommendTeammatesRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User currentUser = userOpt.get();
        
        // Get potential teammates based on criteria
        List<User> potentialTeammates = userRepository.findPotentialTeammates(
                request.getSportType(),
                request.getSkillLevel(),
                request.getLocation(),
                request.getMaxDistance() != null ? request.getMaxDistance() : 50.0,
                request.getGenderPreference(),
                18, // Default min age
                65, // Default max age
                userId
        );
        
        log.info("[DRAFT_MATCH_RECOMMEND_TEAMMATES] Found {} potential teammates for user {}", 
                potentialTeammates.size(), userId);
        
        // Use UnifiedCompatibilityService for teammate recommendations
        try {
            List<Map<String, Object>> recommendations = unifiedCompatibilityService.calculateTeammateCompatibility(
                    currentUser, potentialTeammates, request.getSportType());
            
            // Limit to requested number
            int limit = Math.min(request.getNumberOfTeammates(), recommendations.size());
            List<Map<String, Object>> limitedRecommendations = recommendations.subList(0, limit);
            
            log.info("[DRAFT_MATCH_RECOMMEND_TEAMMATES] Successfully calculated {} teammate recommendations using unified compatibility service", 
                    limitedRecommendations.size());
            
            return limitedRecommendations;
            
        } catch (Exception e) {
            log.error("[DRAFT_MATCH_RECOMMEND_TEAMMATES] Error using unified compatibility service: {}", e.getMessage(), e);
            
            // Fallback to basic recommendations without compatibility scores
            List<Map<String, Object>> fallbackRecommendations = new ArrayList<>();
            
            for (User teammate : potentialTeammates) {
                Map<String, Object> recommendation = new HashMap<>();
                recommendation.put("id", teammate.getId());
                recommendation.put("username", teammate.getUsername());
                recommendation.put("fullName", teammate.getFullName());
                recommendation.put("imageUrl", teammate.getImageUrl());
                recommendation.put("skillLevel", "N/A"); // Skill level not available in User model
                recommendation.put("location", teammate.getAddress());
                recommendation.put("compatibilityScore", 0.5); // Default fallback score
                
                fallbackRecommendations.add(recommendation);
            }
            
            // Limit to requested number
            int limit = Math.min(request.getNumberOfTeammates(), fallbackRecommendations.size());
            return fallbackRecommendations.subList(0, limit);
        }
    }
    
    private void createNotificationForRemovedUser(DraftMatch draftMatch, User removedUser) {
        Notification notification = new Notification();
        notification.setRecipient(removedUser);
        notification.setTitle("Đã bị loại khỏi kèo nháp");
        notification.setContent(String.format("Bạn đã bị loại khỏi kèo nháp %s tại %s.", 
                draftMatch.getSportType(),
                draftMatch.getLocationDescription()));
        notification.setType("ACTION_ERROR");
        notification.setRelatedEntityId(draftMatch.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    
    private void createNotificationForBookingCompleted(DraftMatch draftMatch, User user) {
        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setTitle("Đặt sân thành công");
        notification.setContent(String.format("Kèo nháp %s tại %s đã được đặt sân thành công.", 
                draftMatch.getSportType(),
                draftMatch.getLocationDescription()));
        notification.setType("ACTION_SUCCESS");
        notification.setRelatedEntityId(draftMatch.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    

    
    private List<DraftMatchDto> calculateCompatibilityScores(User user, List<DraftMatchDto> matchDtos, String sportType) {
        try {
            log.info("[COMPATIBILITY_CALCULATION] Calculating compatibility scores for {} draft matches", matchDtos.size());
            
            // Use UnifiedCompatibilityService for consistent scoring
            List<DraftMatchDto> matchesWithScores = unifiedCompatibilityService.calculateDraftMatchCompatibility(user, matchDtos, sportType);
            
            log.info("[COMPATIBILITY_CALCULATION] Successfully calculated compatibility scores for {} draft matches", matchesWithScores.size());
            return matchesWithScores;
            
        } catch (Exception e) {
            log.error("[COMPATIBILITY_CALCULATION] Error calculating compatibility scores: {}", e.getMessage(), e);
            // Return original matches without compatibility scores if calculation fails
            return matchDtos;
        }
    }
}