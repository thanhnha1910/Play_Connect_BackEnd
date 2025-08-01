package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.Notification;
import fpt.aptech.management_field.models.Invitation;
import fpt.aptech.management_field.models.InvitationType;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.models.OpenMatch;
import fpt.aptech.management_field.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getNotificationsForUser(Long userId, Boolean isRead) {
        if (isRead == null) {
            return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
        } else if (isRead) {
            return notificationRepository.findReadByRecipientId(userId);
        } else {
            return notificationRepository.findUnreadByRecipientId(userId);
        }
    }

    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findUnreadByRecipientId(userId);
    }

    public Long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countUnreadByRecipientId(userId);
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllNotificationsAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findUnreadByRecipientId(userId);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }
    
    @Transactional
    public Notification createNotification(Notification notification) {
        // Save notification to database
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send real-time notification via WebSocket
        try {
            Map<String, Object> notificationPayload = new HashMap<>();
            notificationPayload.put("type", savedNotification.getType());
            notificationPayload.put("title", savedNotification.getTitle());
            notificationPayload.put("content", savedNotification.getContent());
            notificationPayload.put("relatedEntityId", savedNotification.getRelatedEntityId());
            notificationPayload.put("createdAt", savedNotification.getCreatedAt());
            notificationPayload.put("isRead", savedNotification.getIsRead());
            
            // Add data object for additional information
            Map<String, Object> data = new HashMap<>();
            if (savedNotification.getRelatedEntityId() != null) {
                if ("DRAFT_MATCH_INTEREST".equals(savedNotification.getType()) ||
                    "DRAFT_MATCH_ACCEPTED".equals(savedNotification.getType()) ||
                    "DRAFT_MATCH_REJECTED".equals(savedNotification.getType()) ||
                    "DRAFT_MATCH_WITHDRAW".equals(savedNotification.getType()) ||
                    "DRAFT_MATCH_CONVERTED".equals(savedNotification.getType()) ||
                    "DRAFT_MATCH_UPDATED".equals(savedNotification.getType())) {
                    data.put("draftMatchId", savedNotification.getRelatedEntityId());
                } else if ("MATCH_JOINED".equals(savedNotification.getType()) ||
                          "MATCH_LEFT".equals(savedNotification.getType())) {
                    data.put("matchId", savedNotification.getRelatedEntityId());
                }
            }
            notificationPayload.put("data", data);
            
            // AUDIT LOG: Track WebSocket notification sending
            System.out.println("🔥 NOTIFICATION AUDIT: Sending WebSocket notification to user " + 
                savedNotification.getRecipient().getId() + " with type: " + savedNotification.getType() + 
                ", title: " + savedNotification.getTitle());
            
            // Send to user's private notification queue
            messagingTemplate.convertAndSendToUser(
                savedNotification.getRecipient().getId().toString(),
                "/queue/notifications",
                notificationPayload
            );
            
            System.out.println("🔥 NOTIFICATION AUDIT: Successfully sent WebSocket notification to user " + 
                savedNotification.getRecipient().getId());
        } catch (Exception e) {
            // Log error but don't fail the notification creation
            System.err.println("🔥 NOTIFICATION AUDIT: Failed to send real-time notification: " + e.getMessage());
            e.printStackTrace();
        }
        
        return savedNotification;
    }
    
    /**
     * Smart notification creation method that understands invitation context
     * This is the single source of truth for creating invitation-related notifications
     */
    @Transactional
    public Notification createNotificationForInvitation(Invitation invitation) {
        User inviter = invitation.getInviter(); // The one who initiated
        User invitee = invitation.getInvitee(); // The one who receives
        OpenMatch match = invitation.getOpenMatch();
        String title;
        String content;
        User recipient;
        
        if (invitation.getType() == InvitationType.REQUEST) {
            // This is a JOIN REQUEST sent by User A (inviter) to User B (invitee/creator)
            recipient = invitee; // The recipient is the match creator
            title = "Yêu cầu tham gia mới";
            content = String.format("%s muốn tham gia trận đấu của bạn tại %s.", 
                    inviter.getFullName(), 
                    getMatchLocationName(match));
        } else { // Type is INVITATION
            // This is an INVITATION sent by User A (inviter/creator) to User B (invitee)
            recipient = invitee;
            title = "Bạn có lời mời mới";
            content = String.format("%s đã mời bạn tham gia trận đấu tại %s.", 
                    inviter.getFullName(), 
                    getMatchLocationName(match));
        }
        
        // Create and save the notification
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRecipient(recipient);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
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
}