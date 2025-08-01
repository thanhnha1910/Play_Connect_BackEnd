package fpt.aptech.management_field.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.aptech.management_field.services.AIRecommendationService;
import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.Invitation;
import fpt.aptech.management_field.models.InvitationStatus;
import fpt.aptech.management_field.models.OpenMatch;
import fpt.aptech.management_field.models.OpenMatchParticipant;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.OpenMatchDto;
import fpt.aptech.management_field.payload.request.CreateOpenMatchRequest;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.InvitationRepository;
import fpt.aptech.management_field.repositories.OpenMatchRepository;
import fpt.aptech.management_field.repositories.OpenMatchParticipantRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OpenMatchService {
    
    private static final Logger log = LoggerFactory.getLogger(OpenMatchService.class);
    
    @Autowired
    private OpenMatchRepository openMatchRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OpenMatchParticipantRepository openMatchParticipantRepository;
    
    @Autowired
    private InvitationRepository invitationRepository;
    
    @Autowired
    private AIRecommendationService aiRecommendationService;
    
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    
    @Autowired
    private NotificationService notificationService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public OpenMatchDto createOpenMatch(CreateOpenMatchRequest request, Long creatorUserId) {
        // Validate booking exists and belongs to user
        Optional<Booking> bookingOpt = bookingRepository.findById(request.getBookingId());
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }
        
        Booking booking = bookingOpt.get();
        if (!booking.getUser().getId().equals(creatorUserId)) {
            throw new RuntimeException("You can only create open matches for your own bookings");
        }
        
        // Check if open match already exists for this booking
        OpenMatch existingMatch = openMatchRepository.findByBookingId(request.getBookingId());
        if (existingMatch != null) {
            throw new RuntimeException("Open match already exists for this booking");
        }
        
        Optional<User> userOpt = userRepository.findById(creatorUserId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        OpenMatch openMatch = new OpenMatch();
        openMatch.setBooking(booking);
        openMatch.setCreatorUser(userOpt.get());
        openMatch.setSportType(request.getSportType());
        openMatch.setSlotsNeeded(request.getSlotsNeeded());
        
        // Convert tags list to JSON string
        try {
            String tagsJson = objectMapper.writeValueAsString(request.getRequiredTags());
            openMatch.setRequiredTags(tagsJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing required tags", e);
        }
        
        openMatch = openMatchRepository.save(openMatch);
        
        OpenMatchDto dto = convertToDto(openMatch);
        
        // Broadcast the new match to all subscribers
        messagingTemplate.convertAndSend("/topic/open-matches", dto);
        
        return dto;
    }
    
    public List<OpenMatchDto> getAllOpenMatches() {
        List<OpenMatch> openMatches = openMatchRepository.findAllOpenMatches();
        List<OpenMatchDto> matchDtos = openMatches.stream().map(this::convertToDto).toList();
        
        // Enrich with AI scores using the centralized method
        try {
            return aiRecommendationService.enrichMatchesWithAiScores(matchDtos, null, null);
        } catch (Exception e) {
            log.warn("Failed to enrich matches with AI scores: {}", e.getMessage());
            return matchDtos;
        }
    }
    
    public List<OpenMatchDto> getAllOpenMatches(Long userId) {
        List<OpenMatch> openMatches = openMatchRepository.findAllOpenMatches();
        List<OpenMatchDto> matchDtos = openMatches.stream().map(match -> {
            // Don't set default compatibility score - let AI service handle it or leave as null
            return convertToDto(match, userId, null);
        }).toList();
        
        // Enrich with AI scores using the centralized method
        try {
            Optional<User> userOpt = userId != null ? userRepository.findById(userId) : Optional.empty();
            return aiRecommendationService.enrichMatchesWithAiScores(matchDtos, userOpt.orElse(null), null);
        } catch (Exception e) {
            log.warn("Failed to enrich matches with AI scores for user {}: {}", userId, e.getMessage());
            return matchDtos;
        }
    }
    
    public List<OpenMatchDto> getOpenMatchesBySport(String sportType) {
        List<OpenMatch> openMatches = openMatchRepository.findOpenMatchesBySportType(sportType);
        List<OpenMatchDto> matchDtos = openMatches.stream().map(this::convertToDto).toList();
        
        // Enrich with AI scores using the centralized method
        try {
            return aiRecommendationService.enrichMatchesWithAiScores(matchDtos, null, sportType);
        } catch (Exception e) {
            log.warn("Failed to enrich matches with AI scores for sport {}: {}", sportType, e.getMessage());
            return matchDtos;
        }
    }
    
    public List<OpenMatchDto> getOpenMatchesBySport(String sportType, Long userId) {
        List<OpenMatch> openMatches = openMatchRepository.findOpenMatchesBySportType(sportType);
        List<OpenMatchDto> matchDtos = openMatches.stream().map(match -> {
            // Don't set default compatibility score - let AI service handle it or leave as null
            return convertToDto(match, userId, null);
        }).toList();
        
        // Enrich with AI scores using the centralized method
        try {
            Optional<User> userOpt = userId != null ? userRepository.findById(userId) : Optional.empty();
            return aiRecommendationService.enrichMatchesWithAiScores(matchDtos, userOpt.orElse(null), sportType);
        } catch (Exception e) {
            log.warn("Failed to enrich matches with AI scores for user {} and sport {}: {}", userId, sportType, e.getMessage());
            return matchDtos;
        }
    }
    
    public List<OpenMatchDto> getRankedOpenMatches(Long userId, String sportType) {
        try {
            // Get user for AI ranking
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                // If user not found, return unranked matches
                return sportType != null ? getOpenMatchesBySport(sportType) : getAllOpenMatches();
            }
            
            User user = userOpt.get();
            List<OpenMatchDto> openMatches;
            
            if (sportType != null && !sportType.isEmpty()) {
                openMatches = getOpenMatchesBySport(sportType, userId);
            } else {
                openMatches = getAllOpenMatches(userId);
            }
            
            // Use AI service to rank matches if available
            if (aiRecommendationService != null && sportType != null) {
                try {
                    // Check if AI service is available
                    boolean isAIAvailable = aiRecommendationService.isAIServiceAvailable();
                    System.out.println("[DEBUG] AI Service available: " + isAIAvailable);
                    
                    if (isAIAvailable) {
                        // Try hybrid ranking first, fallback to legacy if needed
                        List<OpenMatchDto> rankedMatches;
                        try {
                            rankedMatches = aiRecommendationService.rankOpenMatchesHybrid(user, openMatches, sportType);
                            System.out.println("[DEBUG] Using hybrid ranking model");
                        } catch (Exception hybridException) {
                            System.out.println("[DEBUG] Hybrid ranking failed, falling back to legacy: " + hybridException.getMessage());
                            rankedMatches = aiRecommendationService.rankOpenMatches(user, openMatches, sportType);
                        }
                        
                        // Debug logging to verify compatibilityScore is being set
                        System.out.println("=== DEBUG: Ranked matches returned from AI service ===");
                        for (OpenMatchDto match : rankedMatches) {
                            System.out.println(String.format("Match ID: %d, CompatibilityScore: %s, CreatorId: %d", 
                                match.getId(), 
                                match.getCompatibilityScore() != null ? match.getCompatibilityScore().toString() : "NULL",
                                match.getCreatorUserId()));
                        }
                        System.out.println("=== END DEBUG ===");
                        
                        // Add production logging for compatibility score verification
                        log.info("[COMPATIBILITY_AUDIT] Final ranked matches for user {}: {} matches returned", userId, rankedMatches.size());
                        for (OpenMatchDto match : rankedMatches) {
                            log.info("[COMPATIBILITY_AUDIT] Match ID: {}, CompatibilityScore: {}, CreatorId: {}", 
                                match.getId(), 
                                match.getCompatibilityScore(), 
                                match.getCreatorUserId());
                        }
                        
                        // AI service already returns properly formatted DTOs with compatibility scores
                        // No need to convert again - just return the ranked matches directly
                        return rankedMatches;
                    } else {
                        System.out.println("[DEBUG] AI Service not available, returning unranked matches");
                    }
                } catch (Exception aiException) {
                    System.err.println("[ERROR] AI Service failed: " + aiException.getMessage());
                    aiException.printStackTrace();
                    // Continue with unranked matches if AI service fails
                }
            } else {
                System.out.println("[DEBUG] AI Service is null or sportType is null");
            }
            
            return openMatches;
            
        } catch (Exception e) {
            System.err.println("[ERROR] Error in getRankedOpenMatches: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: return empty list or basic matches without user context
            try {
                if (sportType != null && !sportType.isEmpty()) {
                    return getOpenMatchesBySport(sportType);
                } else {
                    return getAllOpenMatches();
                }
            } catch (Exception fallbackException) {
                System.err.println("[ERROR] Fallback also failed: " + fallbackException.getMessage());
                return new ArrayList<>();
            }
        }
    }
    
    public List<OpenMatchDto> getUserOpenMatches(Long userId) {
        List<OpenMatch> openMatches = openMatchRepository.findByCreatorUserId(userId);
        List<OpenMatchDto> matchDtos = openMatches.stream().map(this::convertToDto).toList();
        
        // Enrich user's own matches with AI scores
        if (userId != null && !matchDtos.isEmpty()) {
            try {
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    // Use "general" as default sport type for user's own matches
                    List<OpenMatchDto> enrichedMatches = aiRecommendationService.enrichMatchesWithAiScores(matchDtos, user, "general");
                    log.info("[AI_ENRICHMENT] getUserOpenMatches(userId={}) enriched {} matches with AI scores", userId, enrichedMatches.size());
                    return enrichedMatches;
                } else {
                    log.warn("[AI_ENRICHMENT] User {} not found, returning matches without AI enrichment", userId);
                }
            } catch (Exception e) {
                log.error("[AI_ENRICHMENT] Error enriching user matches for user {}: {}", userId, e.getMessage(), e);
            }
        }
        
        return matchDtos;
    }
    
    public void closeOpenMatch(Long matchId, Long userId) {
        Optional<OpenMatch> matchOpt = openMatchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            throw new RuntimeException("Open match not found");
        }
        
        OpenMatch match = matchOpt.get();
        if (!match.getCreatorUser().getId().equals(userId)) {
            throw new RuntimeException("You can only close your own open matches");
        }
        
        match.setStatus("CLOSED");
        openMatchRepository.save(match);
        
        // Broadcast the updated match to all subscribers
        OpenMatchDto updatedMatchDto = convertToDto(match, userId);
        messagingTemplate.convertAndSend("/topic/match/" + matchId, updatedMatchDto);
    }
    
    public void joinOpenMatch(Long matchId, Long userId) {
        Optional<OpenMatch> matchOpt = openMatchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            throw new RuntimeException("Open match not found");
        }
        
        OpenMatch match = matchOpt.get();
        if (!"OPEN".equals(match.getStatus())) {
            throw new RuntimeException("This match is no longer open for joining");
        }
        
        // Check if user is the creator (creators cannot join their own matches)
        if (match.getCreatorUser().getId().equals(userId)) {
            throw new RuntimeException("You cannot join your own match");
        }
        
        // Check if user already joined
        Optional<OpenMatchParticipant> existingParticipant = 
            openMatchParticipantRepository.findByOpenMatchIdAndUserId(matchId, userId);
        if (existingParticipant.isPresent()) {
            throw new RuntimeException("You have already joined this match");
        }
        
        // Check if match is full
        Long currentParticipants = openMatchParticipantRepository.countByOpenMatchId(matchId);
        if (currentParticipants >= match.getSlotsNeeded()) {
            throw new RuntimeException("This match is already full");
        }
        
        // Get user
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        // Create participant record
        OpenMatchParticipant participant = new OpenMatchParticipant();
        participant.setOpenMatch(match);
        participant.setUser(userOpt.get());
        openMatchParticipantRepository.save(participant);
        
        // Get joining user for notification
        User joiningUser = userOpt.get();
        
        // Create notification for match creator about user joining
        if (!match.getCreatorUser().getId().equals(userId)) {
            try {
                fpt.aptech.management_field.models.Notification notification = new fpt.aptech.management_field.models.Notification();
                notification.setTitle("Thành viên mới tham gia");
                notification.setContent(String.format("Người chơi %s đã tham gia trận của bạn.", 
                        joiningUser.getFullName()));
                notification.setRecipient(match.getCreatorUser());
                notification.setType("INFO_UPDATE");
                notification.setRelatedEntityId(matchId);
                notification.setIsRead(false);
                notification.setCreatedAt(java.time.LocalDateTime.now());
                
                notificationService.createNotification(notification);
                
                // Send real-time notification to match creator
                messagingTemplate.convertAndSendToUser(
                    match.getCreatorUser().getId().toString(),
                    "/queue/notifications",
                    notification
                );
            } catch (Exception e) {
                // Log error but don't fail the join operation
                System.err.println("Failed to create join notification: " + e.getMessage());
            }
        }
        
        // Create notification for joining user
        try {
            fpt.aptech.management_field.models.Notification userNotification = new fpt.aptech.management_field.models.Notification();
            userNotification.setTitle("Tham gia trận đấu thành công");
            userNotification.setContent("Bạn đã tham gia trận thành công.");
            userNotification.setRecipient(joiningUser);
            userNotification.setType("ACTION_SUCCESS");
            userNotification.setRelatedEntityId(matchId);
            userNotification.setIsRead(false);
            userNotification.setCreatedAt(java.time.LocalDateTime.now());
            
            notificationService.createNotification(userNotification);
            
            // Send real-time notification to joining user
            messagingTemplate.convertAndSendToUser(
                joiningUser.getId().toString(),
                "/queue/notifications",
                userNotification
            );
        } catch (Exception e) {
            System.err.println("Failed to create user join notification: " + e.getMessage());
        }
        
        // Update match status if full
        Long newParticipantCount = openMatchParticipantRepository.countByOpenMatchId(matchId);
        if (newParticipantCount >= match.getSlotsNeeded()) {
            match.setStatus("FULL");
            openMatchRepository.save(match);
        }
        
        // Broadcast the updated match to all subscribers
        OpenMatchDto updatedMatchDto = convertToDto(match, userId);
        messagingTemplate.convertAndSend("/topic/match/" + matchId, updatedMatchDto);
    }
    
    public void leaveOpenMatch(Long matchId, Long userId) {
        Optional<OpenMatch> matchOpt = openMatchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            throw new RuntimeException("Open match not found");
        }

        OpenMatch match = matchOpt.get();

        // Find participant record
        Optional<OpenMatchParticipant> participantOpt = 
            openMatchParticipantRepository.findByOpenMatchIdAndUserId(matchId, userId);
        if (participantOpt.isEmpty()) {
            throw new RuntimeException("You are not a participant in this match");
        }

        // Get user info for notification
        Optional<User> leavingUserOpt = userRepository.findById(userId);
        if (leavingUserOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User leavingUser = leavingUserOpt.get();

        // Remove participant
        openMatchParticipantRepository.delete(participantOpt.get());

        // Update corresponding invitation status to CANCELLED
        Optional<Invitation> invitationOpt = invitationRepository.findByInviterAndInviteeAndOpenMatch(
                userId, match.getCreatorUser().getId(), matchId);
        if (invitationOpt.isPresent()) {
            Invitation invitation = invitationOpt.get();
            invitation.setStatus(InvitationStatus.CANCELLED);
            invitationRepository.save(invitation);
        }

        // Create notification for match creator about user leaving
        if (!match.getCreatorUser().getId().equals(userId)) {
            try {
                fpt.aptech.management_field.models.Notification notification = new fpt.aptech.management_field.models.Notification();
                notification.setTitle("Thành viên rời khỏi đội");
                notification.setContent(String.format("Người chơi %s đã rời khỏi trận của bạn.", 
                        leavingUser.getFullName()));
                notification.setRecipient(match.getCreatorUser());
                notification.setType("INFO_UPDATE");
                notification.setRelatedEntityId(matchId);
                notification.setIsRead(false);
                notification.setCreatedAt(java.time.LocalDateTime.now());
                
                notificationService.createNotification(notification);
                
                // Send real-time notification to match creator
                messagingTemplate.convertAndSendToUser(
                    match.getCreatorUser().getId().toString(),
                    "/queue/notifications",
                    notification
                );
            } catch (Exception e) {
                // Log error but don't fail the leave operation
                System.err.println("Failed to create leave notification: " + e.getMessage());
            }
        }
        
        // Create notification for leaving user
        try {
            fpt.aptech.management_field.models.Notification userNotification = new fpt.aptech.management_field.models.Notification();
            userNotification.setTitle("Rời khỏi trận đấu thành công");
            userNotification.setContent("Bạn đã rời trận thành công.");
            userNotification.setRecipient(leavingUser);
            userNotification.setType("ACTION_SUCCESS");
            userNotification.setRelatedEntityId(matchId);
            userNotification.setIsRead(false);
            userNotification.setCreatedAt(java.time.LocalDateTime.now());
            
            notificationService.createNotification(userNotification);
            
            // Send real-time notification to leaving user
            messagingTemplate.convertAndSendToUser(
                leavingUser.getId().toString(),
                "/queue/notifications",
                userNotification
            );
        } catch (Exception e) {
            System.err.println("Failed to create user leave notification: " + e.getMessage());
        }

        // Update match status if it was full
        if ("FULL".equals(match.getStatus())) {
            match.setStatus("OPEN");
            openMatchRepository.save(match);
        }
        
        // Broadcast the updated match to all subscribers
        OpenMatchDto updatedMatchDto = convertToDto(match, userId);
        messagingTemplate.convertAndSend("/topic/match/" + matchId, updatedMatchDto);
    }
    
    /**
     * Helper method to get match location name safely
     */
    private String getMatchLocationName(OpenMatch match) {
        if (match.getBooking() != null && 
            match.getBooking().getField() != null && 
            match.getBooking().getField().getName() != null) {
            return match.getBooking().getField().getName();
        }
        return "sân thể thao";
    }
    
    public OpenMatchDto getOpenMatchByBooking(Long bookingId) {
        return getOpenMatchByBooking(bookingId, null);
    }
    
    public OpenMatchDto getOpenMatchByBooking(Long bookingId, Long userId) {
        OpenMatch openMatch = openMatchRepository.findByBookingId(bookingId);
        if (openMatch == null) {
            throw new RuntimeException("Open match not found for booking ID: " + bookingId);
        }
        return convertToDto(openMatch, userId);
    }
    
    private OpenMatchDto convertToDto(OpenMatch openMatch) {
        return convertToDto(openMatch, null);
    }
    
    private OpenMatchDto convertToDto(OpenMatch openMatch, Long userId) {
        return convertToDto(openMatch, userId, null);
    }
    
    private OpenMatchDto convertToDto(OpenMatch openMatch, Long userId, Double compatibilityScore) {
        OpenMatchDto dto = new OpenMatchDto();
        dto.setId(openMatch.getId());
        dto.setBookingId(openMatch.getBooking().getBookingId());
        dto.setCreatorUserId(openMatch.getCreatorUser().getId());
        dto.setCreatorUserName(openMatch.getCreatorUser().getFullName());
        
        // Set creator avatar URL if available
        if (openMatch.getCreatorUser().getImageUrl() != null) {
            dto.setCreatorAvatarUrl(openMatch.getCreatorUser().getImageUrl());
        }
        
        dto.setSportType(openMatch.getSportType());
        dto.setSlotsNeeded(openMatch.getSlotsNeeded());
        dto.setStatus(openMatch.getStatus());
        dto.setCreatedAt(openMatch.getCreatedAt());
        dto.setFieldName(openMatch.getBooking().getField().getName());
        
        // Set location information
        if (openMatch.getBooking().getField().getLocation() != null) {
            dto.setLocationName(openMatch.getBooking().getField().getLocation().getName());
            dto.setLocationAddress(openMatch.getBooking().getField().getLocation().getAddress());
        }
        
        // Set booking time information
        dto.setStartTime(openMatch.getBooking().getFromTime());
        dto.setEndTime(openMatch.getBooking().getToTime());
        
        // Extract booking date from start time
        if (openMatch.getBooking().getFromTime() != null) {
            dto.setBookingDate(openMatch.getBooking().getFromTime().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        
        // Convert JSON string back to list
        try {
            List<String> tags = objectMapper.readValue(openMatch.getRequiredTags(), new TypeReference<List<String>>() {});
            dto.setRequiredTags(tags);
        } catch (JsonProcessingException e) {
            dto.setRequiredTags(new ArrayList<>());
        }
        
        // Set participant count
        Long participantCount = openMatchParticipantRepository.countByOpenMatchId(openMatch.getId());
        dto.setCurrentParticipants(participantCount.intValue());
        
        // Set participant IDs
        List<OpenMatchParticipant> participants = openMatchParticipantRepository.findByOpenMatchId(openMatch.getId());
        List<Long> participantIds = participants.stream()
            .map(participant -> participant.getUser().getId())
            .toList();
        dto.setParticipantIds(participantIds);
        
        // Set compatibility score if provided
        if (compatibilityScore != null) {
            dto.setCompatibilityScore(compatibilityScore);
        }
        
        // Calculate currentUserJoinStatus if userId is provided
        if (userId != null) {
            dto.setCurrentUserJoinStatus(calculateUserJoinStatus(openMatch, userId));
        } else {
            dto.setCurrentUserJoinStatus("NOT_JOINED");
        }
        
        return dto;
    }
    
    private String calculateUserJoinStatus(OpenMatch openMatch, Long userId) {
        // Check if user is already a participant
        Optional<OpenMatchParticipant> participant = 
            openMatchParticipantRepository.findByOpenMatchIdAndUserId(openMatch.getId(), userId);
        if (participant.isPresent()) {
            return "JOINED";
        }
        
        // Check if user has a pending join request
        Optional<Invitation> invitation = invitationRepository.findByInviterAndInviteeAndOpenMatch(
                userId, openMatch.getCreatorUser().getId(), openMatch.getId());
        if (invitation.isPresent() && invitation.get().getStatus() == InvitationStatus.PENDING) {
            return "REQUEST_PENDING";
        }
        
        return "NOT_JOINED";
    }
}