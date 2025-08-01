package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.*;
import fpt.aptech.management_field.payload.dtos.InvitationDto;
import fpt.aptech.management_field.payload.dtos.SendInvitationRequest;
import fpt.aptech.management_field.repositories.InvitationRepository;
import fpt.aptech.management_field.repositories.OpenMatchRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvitationService {
    
    @Autowired
    private InvitationRepository invitationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OpenMatchRepository openMatchRepository;
    
    @Autowired
    private OpenMatchService openMatchService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    
    @Transactional
    public InvitationDto sendInvitation(SendInvitationRequest request, Long inviterId) {
        // Validate inviter exists
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));
        
        // Validate invitee exists
        User invitee = userRepository.findById(request.getInviteeId())
                .orElseThrow(() -> new RuntimeException("Invitee not found"));
        
        // Validate open match exists
        OpenMatch openMatch = openMatchRepository.findById(request.getOpenMatchId())
                .orElseThrow(() -> new RuntimeException("Open match not found"));
        
        // Check if inviter is the creator of the open match
        if (!openMatch.getCreatorUser().getId().equals(inviterId)) {
            throw new RuntimeException("You can only send invitations for your own matches");
        }
        
        // Check if invitee is the same as inviter
        if (inviterId.equals(request.getInviteeId())) {
            throw new RuntimeException("You cannot invite yourself");
        }
        
        // Check if invitation already exists
        Optional<Invitation> existingInvitation = invitationRepository
                .findByInviterAndInviteeAndOpenMatch(inviterId, request.getInviteeId(), request.getOpenMatchId());
        if (existingInvitation.isPresent()) {
            throw new RuntimeException("Invitation already sent to this user for this match");
        }
        
        // Check if match is still open
        if (!"OPEN".equals(openMatch.getStatus())) {
            throw new RuntimeException("This match is no longer open for invitations");
        }
        
        // Create invitation
        Invitation invitation = new Invitation();
        invitation.setInviter(inviter);
        invitation.setInvitee(invitee);
        invitation.setOpenMatch(openMatch);
        invitation.setType(InvitationType.INVITATION);
        invitation.setStatus(InvitationStatus.PENDING);
        
        invitation = invitationRepository.save(invitation);
        
        // Send notification to invitee using centralized service
        notificationService.createNotificationForInvitation(invitation);
        
        // Convert to DTO for return and WebSocket broadcast
        InvitationDto invitationDto = convertToDto(invitation);
        
        // Send real-time update to invitee
        messagingTemplate.convertAndSend("/user/" + invitation.getInvitee().getId() + "/queue/invitations", invitationDto);
        
        return invitationDto;
    }
    
    @Transactional
    public InvitationDto sendJoinRequest(Long openMatchId, Long requesterId) {
        // Validate requester exists
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate open match exists
        OpenMatch openMatch = openMatchRepository.findById(openMatchId)
                .orElseThrow(() -> new RuntimeException("Open match not found"));
        
        // Check if requester is the creator of the open match
        if (openMatch.getCreatorUser().getId().equals(requesterId)) {
            throw new RuntimeException("You cannot request to join your own match");
        }
        
        // Check if request already exists
        Optional<Invitation> existingRequest = invitationRepository
                .findByInviterAndInviteeAndOpenMatch(requesterId, openMatch.getCreatorUser().getId(), openMatchId);
        if (existingRequest.isPresent()) {
            Invitation existing = existingRequest.get();
            // If request is pending, throw error
            if (existing.getStatus() == InvitationStatus.PENDING) {
                throw new RuntimeException("Join request already sent for this match");
            }
            // If request was rejected or cancelled, delete it to allow new request
            if (existing.getStatus() == InvitationStatus.REJECTED || 
                existing.getStatus() == InvitationStatus.CANCELLED) {
                invitationRepository.delete(existing);
            }
        }
        
        // Check if match is still open
        if (!"OPEN".equals(openMatch.getStatus())) {
            throw new RuntimeException("This match is no longer open for join requests");
        }
        
        // Create join request (inviter is the requester, invitee is the match creator)
        Invitation joinRequest = new Invitation();
        joinRequest.setInviter(requester);
        joinRequest.setInvitee(openMatch.getCreatorUser());
        joinRequest.setOpenMatch(openMatch);
        joinRequest.setType(InvitationType.REQUEST);
        joinRequest.setStatus(InvitationStatus.PENDING);
        
        joinRequest = invitationRepository.save(joinRequest);
        
        // Send notification to match creator using centralized service
        notificationService.createNotificationForInvitation(joinRequest);
        
        // === BACKEND TRACE: Send ACTION_SUCCESS toast notification to the requester ===
        System.out.println("[BACKEND_TRACE] STEP 1: Entered sendJoinRequest method in InvitationService for matchId: " + openMatch.getId());
        System.out.println("[BACKEND_TRACE] STEP 2: Successfully saved join request to database.");
        
        // Create and send ACTION_SUCCESS notification to the user who made the join request
        System.out.println("[BACKEND_TRACE] STEP 3: Attempting to send ACTION_SUCCESS toast notification to user ID: " + requester.getId());
        
        Notification actionSuccessNotification = new Notification();
        actionSuccessNotification.setRecipient(requester);
        actionSuccessNotification.setTitle("Đã gửi yêu cầu");
        actionSuccessNotification.setContent("Yêu cầu tham gia của bạn đã được gửi đi. Vui lòng chờ chủ sân duyệt.");
        actionSuccessNotification.setType("ACTION_SUCCESS");
        actionSuccessNotification.setRelatedEntityId(joinRequest.getId());
        actionSuccessNotification.setCreatedAt(LocalDateTime.now());
        actionSuccessNotification.setIsRead(false);
        
        // Save and send the ACTION_SUCCESS notification
        notificationService.createNotification(actionSuccessNotification);
        
        System.out.println("[BACKEND_TRACE] STEP 4: Successfully called notificationService to send toast.");
        
        // Convert to DTO for return and WebSocket broadcast
        InvitationDto joinRequestDto = convertToDto(joinRequest);
        
        // Send real-time update to match creator
        messagingTemplate.convertAndSend("/user/" + joinRequest.getInvitee().getId() + "/queue/invitations", joinRequestDto);
        
        return joinRequestDto;
    }
    
    public List<InvitationDto> getReceivedInvitations(Long userId) {
        List<Invitation> invitations = invitationRepository.findPendingReceivedInvitations(userId);
        return invitations.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public List<InvitationDto> getSentInvitations(Long userId) {
        List<Invitation> invitations = invitationRepository.findSentInvitationsByUserId(userId);
        return invitations.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public List<InvitationDto> getReceivedJoinRequests(Long userId) {
        List<Invitation> joinRequests = invitationRepository.findPendingReceivedJoinRequests(userId);
        return joinRequests.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    public List<InvitationDto> getSentJoinRequests(Long userId) {
        List<Invitation> joinRequests = invitationRepository.findSentJoinRequestsByUserId(userId);
        return joinRequests.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    // UNIFIED METHODS - Single source of truth for frontend
    
    /**
     * Get all received items (invitations + join requests) for a user
     * This replaces the need for separate getReceivedInvitations() and getReceivedJoinRequests() calls
     */
    public List<InvitationDto> getAllReceivedItems(Long userId) {
        List<Invitation> allReceived = invitationRepository.findAllReceivedItems(userId);
        return allReceived.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * Get all sent items (invitations + join requests) for a user
     * This replaces the need for separate getSentInvitations() and getSentJoinRequests() calls
     */
    public List<InvitationDto> getAllSentItems(Long userId) {
        List<Invitation> allSent = invitationRepository.findAllSentItems(userId);
        return allSent.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Transactional
    public InvitationDto acceptInvitation(Long invitationId, Long currentUserId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        // Verify the current user has permission to accept (they must be the invitee)
        if (!invitation.getInvitee().getId().equals(currentUserId)) {
            throw new RuntimeException("You don't have permission to accept this invitation");
        }
        
        // Check if invitation is still pending
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("This invitation has already been processed");
        }
        
        // Change the invitation status to ACCEPTED
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation = invitationRepository.save(invitation);
        
        // Add the user to the match's participant list
        try {
            if (invitation.getType() == InvitationType.INVITATION) {
                // For invitations, the invitee joins the match
                openMatchService.joinOpenMatch(invitation.getOpenMatch().getId(), invitation.getInvitee().getId());
            } else {
                // For requests, the inviter (requester) joins the match
                openMatchService.joinOpenMatch(invitation.getOpenMatch().getId(), invitation.getInviter().getId());
            }
        } catch (Exception e) {
            // If joining fails, revert the invitation status
            invitation.setStatus(InvitationStatus.PENDING);
            invitationRepository.save(invitation);
            throw new RuntimeException("Failed to join the match: " + e.getMessage());
        }
        
        // Send notification back to the original inviter
        sendAcceptanceNotification(invitation);
        
        // Convert to DTO for return and WebSocket broadcast
        InvitationDto invitationDto = convertToDto(invitation);
        
        // Send real-time update to both inviter and invitee
        messagingTemplate.convertAndSend("/user/" + invitation.getInviter().getId() + "/queue/invitations", invitationDto);
        messagingTemplate.convertAndSend("/user/" + invitation.getInvitee().getId() + "/queue/invitations", invitationDto);
        
        return invitationDto;
    }
    
    @Transactional
    public InvitationDto rejectInvitation(Long invitationId, Long currentUserId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        // Verify the current user has permission to reject (they must be the invitee)
        if (!invitation.getInvitee().getId().equals(currentUserId)) {
            throw new RuntimeException("You don't have permission to reject this invitation");
        }
        
        // Check if invitation is still pending
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("This invitation has already been processed");
        }
        
        // Change the invitation status to REJECTED
        invitation.setStatus(InvitationStatus.REJECTED);
        invitation = invitationRepository.save(invitation);
        
        // Send notification back to the original inviter
        sendRejectionNotification(invitation);
        
        // Convert to DTO for return and WebSocket broadcast
        InvitationDto invitationDto = convertToDto(invitation);
        
        // Send real-time update to both inviter and invitee
        messagingTemplate.convertAndSend("/user/" + invitation.getInviter().getId() + "/queue/invitations", invitationDto);
        messagingTemplate.convertAndSend("/user/" + invitation.getInvitee().getId() + "/queue/invitations", invitationDto);
        
        return invitationDto;
    }
    
    @Transactional
    public InvitationDto acceptJoinRequest(Long requestId, Long currentUserId) {
        Invitation joinRequest = invitationRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Join request not found"));
        
        // Verify the current user has permission to accept (they must be the invitee - match creator)
        if (!joinRequest.getInvitee().getId().equals(currentUserId)) {
            throw new RuntimeException("You don't have permission to accept this join request");
        }
        
        // Check if request is still pending
        if (joinRequest.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("This join request has already been processed");
        }
        
        // Change the request status to ACCEPTED
        joinRequest.setStatus(InvitationStatus.ACCEPTED);
        joinRequest = invitationRepository.save(joinRequest);
        
        // Add the requester to the match's participant list
        try {
            openMatchService.joinOpenMatch(joinRequest.getOpenMatch().getId(), joinRequest.getInviter().getId());
        } catch (Exception e) {
            // If joining fails, revert the request status
            joinRequest.setStatus(InvitationStatus.PENDING);
            invitationRepository.save(joinRequest);
            throw new RuntimeException("Failed to join the match: " + e.getMessage());
        }
        
        // Send notification back to the requester
        sendAcceptanceNotification(joinRequest);
        
        // Convert to DTO for return and WebSocket broadcast
        InvitationDto joinRequestDto = convertToDto(joinRequest);
        
        // Send real-time update to both requester and match creator
        messagingTemplate.convertAndSend("/user/" + joinRequest.getInviter().getId() + "/queue/invitations", joinRequestDto);
        messagingTemplate.convertAndSend("/user/" + joinRequest.getInvitee().getId() + "/queue/invitations", joinRequestDto);
        
        return joinRequestDto;
    }
    
    @Transactional
    public InvitationDto rejectJoinRequest(Long requestId, Long currentUserId) {
        Invitation joinRequest = invitationRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Join request not found"));
        
        // Verify the current user has permission to reject (they must be the invitee - match creator)
        if (!joinRequest.getInvitee().getId().equals(currentUserId)) {
            throw new RuntimeException("You don't have permission to reject this join request");
        }
        
        // Check if request is still pending
        if (joinRequest.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("This join request has already been processed");
        }
        
        // Change the request status to REJECTED
        joinRequest.setStatus(InvitationStatus.REJECTED);
        joinRequest = invitationRepository.save(joinRequest);
        
        // Send notification back to the requester
        sendRejectionNotification(joinRequest);
        
        // Convert to DTO for return and WebSocket broadcast
        InvitationDto joinRequestDto = convertToDto(joinRequest);
        
        // Send real-time update to both requester and match creator
        messagingTemplate.convertAndSend("/user/" + joinRequest.getInviter().getId() + "/queue/invitations", joinRequestDto);
        messagingTemplate.convertAndSend("/user/" + joinRequest.getInvitee().getId() + "/queue/invitations", joinRequestDto);
        
        return joinRequestDto;
    }
    
    // Old notification methods removed - now using centralized NotificationService.createNotificationForInvitation()
    
    private void sendAcceptanceNotification(Invitation invitation) {
        // Create acceptance notification using centralized service
        Notification notification = new Notification();
        notification.setRecipient(invitation.getInviter());
        
        String title;
        String content;
        String fieldName = getMatchLocationName(invitation.getOpenMatch());
        
        if (invitation.getType() == InvitationType.INVITATION) {
            title = "Lời mời được chấp nhận";
            content = String.format("%s đã chấp nhận lời mời tham gia trận đấu tại %s.", 
                    invitation.getInvitee().getFullName(), fieldName);
        } else {
            title = "Yêu cầu được chấp nhận";
            content = String.format("Bạn đã được chấp nhận tham gia trận đấu tại %s.", fieldName);
        }
        
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("REQUEST_ACCEPTED");
        notification.setRelatedEntityId(invitation.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }

    private void sendRejectionNotification(Invitation invitation) {
        // Create rejection notification using centralized service
        Notification notification = new Notification();
        notification.setRecipient(invitation.getInviter());
        notification.setTitle("Lời mời bị từ chối");
        
        String content;
        String fieldName = getMatchLocationName(invitation.getOpenMatch());
        
        if (invitation.getType() == InvitationType.INVITATION) {
            content = String.format("%s đã từ chối lời mời tham gia trận đấu tại %s.", 
                    invitation.getInvitee().getFullName(), fieldName);
        } else {
            content = String.format("Yêu cầu tham gia trận đấu tại %s của bạn đã bị từ chối.", fieldName);
        }
        
        notification.setContent(content);
        notification.setType("REQUEST_REJECTED");
        notification.setRelatedEntityId(invitation.getId());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
    
    private String getMatchLocationName(OpenMatch openMatch) {
        if (openMatch.getBooking() != null && 
            openMatch.getBooking().getField() != null && 
            openMatch.getBooking().getField().getName() != null) {
            return openMatch.getBooking().getField().getName();
        }
        return "sân đấu";
    }
    
    private InvitationDto convertToDto(Invitation invitation) {
        InvitationDto dto = new InvitationDto();
        dto.setId(invitation.getId());
        dto.setInviterId(invitation.getInviter().getId());
        dto.setInviterName(invitation.getInviter().getFullName());
        dto.setInviterProfilePicture(invitation.getInviter().getProfilePicture());
        dto.setInviteeId(invitation.getInvitee().getId());
        dto.setInviteeName(invitation.getInvitee().getFullName());
        dto.setInviteeProfilePicture(invitation.getInvitee().getProfilePicture());
        dto.setOpenMatchId(invitation.getOpenMatch().getId());
        dto.setOpenMatchTitle(invitation.getOpenMatch().getSportType() + " Match");
        dto.setSportType(invitation.getOpenMatch().getSportType());
        
        // Get field name from booking
        if (invitation.getOpenMatch().getBooking() != null && 
            invitation.getOpenMatch().getBooking().getField() != null) {
            dto.setFieldName(invitation.getOpenMatch().getBooking().getField().getName());
        }
        
        // Get match date time from booking
        if (invitation.getOpenMatch().getBooking() != null) {
            dto.setMatchDateTime(invitation.getOpenMatch().getBooking().getFromTime() != null ? 
                    LocalDateTime.ofInstant(invitation.getOpenMatch().getBooking().getFromTime(), 
                            java.time.ZoneId.systemDefault()) : null);
        }
        
        dto.setType(invitation.getType());
        dto.setStatus(invitation.getStatus());
        dto.setCreatedAt(invitation.getCreatedAt());
        dto.setUpdatedAt(invitation.getUpdatedAt());
        
        return dto;
    }
}