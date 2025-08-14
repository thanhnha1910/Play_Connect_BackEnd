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
    
    @Autowired
    private EmailService emailService;

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
     * Create notification for tournament registration - sends both in-app and email notification
     */
    @Transactional
    public Notification createTournamentRegistrationNotificationForOwner(User fieldOwner, String teamName, String tournamentName, String fieldName, Long tournamentId) {
        // Create in-app notification
        Notification notification = new Notification();
        notification.setRecipient(fieldOwner);
        notification.setTitle("Đội mới đăng ký giải đấu");
        notification.setContent(String.format("Đội '%s' đã đăng ký và thanh toán thành công cho giải đấu '%s' tại sân %s.", teamName, tournamentName, fieldName));
        notification.setType("TOURNAMENT_REGISTRATION");
        notification.setRelatedEntityId(tournamentId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        
        Notification savedNotification = createNotification(notification);
        
        // Send email notification
        try {
            String emailSubject = "Thông báo đăng ký giải đấu mới - " + tournamentName;
            String emailContent = String.format(
                "Xin chào %s,\n\n" +
                "Có đội mới đăng ký giải đấu của bạn:\n" +
                "- Tên đội: %s\n" +
                "- Giải đấu: %s\n" +
                "- Sân: %s\n\n" +
                "Đội đã thanh toán thành công và được xác nhận tham gia.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ PlayerConnect",
                fieldOwner.getFullName(),
                teamName,
                tournamentName,
                fieldName
            );
            
            emailService.sendEmail(fieldOwner.getEmail(), emailSubject, emailContent);
        } catch (Exception e) {
            System.err.println("Failed to send tournament registration email notification: " + e.getMessage());
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
     * Create notification for new booking - sends both in-app and email notification
     */
    @Transactional
    public Notification createBookingNotificationForOwner(User fieldOwner, String fieldName, String customerName, String bookingTime, Long bookingId) {
        System.out.println("🔥 BOOKING NOTIFICATION DEBUG: Starting createBookingNotificationForOwner");
        System.out.println("🔥 BOOKING NOTIFICATION DEBUG: Field Owner: " + fieldOwner.getFullName() + ", Email: " + fieldOwner.getEmail());
        
        try {
            // Create in-app notification
            Notification notification = new Notification();
            notification.setRecipient(fieldOwner);
            notification.setTitle("Đặt sân mới");
            notification.setContent(String.format("%s đã đặt sân %s vào lúc %s.", customerName, fieldName, bookingTime));
            notification.setType("NEW_BOOKING");
            notification.setRelatedEntityId(bookingId);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setIsRead(false);
            
            System.out.println("🔥 BOOKING NOTIFICATION DEBUG: About to call createNotification");
            Notification savedNotification = createNotification(notification);
            System.out.println("🔥 BOOKING NOTIFICATION DEBUG: Successfully created notification with ID: " + savedNotification.getId());
            
            // Send email notification
            try {
                System.out.println("🔥 BOOKING NOTIFICATION DEBUG: About to send email");
                String emailSubject = "Thông báo đặt sân mới - " + fieldName;
                String emailContent = String.format(
                    "Xin chào %s,\n\n" +
                    "Bạn có một đặt sân mới:\n" +
                    "- Khách hàng: %s\n" +
                    "- Sân: %s\n" +
                    "- Thời gian: %s\n\n" +
                    "Vui lòng kiểm tra hệ thống để xem chi tiết.\n\n" +
                    "Trân trọng,\n" +
                    "Đội ngũ PlayerConnect",
                    fieldOwner.getFullName(),
                    customerName,
                    fieldName,
                    bookingTime
                );
                
                emailService.sendEmail(fieldOwner.getEmail(), emailSubject, emailContent);
                System.out.println("🔥 BOOKING NOTIFICATION DEBUG: Email sent successfully");
            } catch (Exception e) {
                System.err.println("🔥 BOOKING NOTIFICATION DEBUG: Failed to send booking email notification: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("🔥 BOOKING NOTIFICATION DEBUG: Completed successfully");
            return savedNotification;
        } catch (Exception e) {
            System.err.println("🔥 BOOKING NOTIFICATION DEBUG: CRITICAL ERROR in createBookingNotificationForOwner: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Create notification for new tournament - sends both in-app and email notification
     */
    @Transactional
    public Notification createTournamentNotificationForOwner(User fieldOwner, String tournamentName, String fieldName, String startDate, Long tournamentId) {
        // Create in-app notification
        Notification notification = new Notification();
        notification.setRecipient(fieldOwner);
        notification.setTitle("Giải đấu mới được tạo");
        notification.setContent(String.format("Giải đấu '%s' đã được tạo tại sân %s, bắt đầu từ %s.", tournamentName, fieldName, startDate));
        notification.setType("NEW_TOURNAMENT");
        notification.setRelatedEntityId(tournamentId);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        
        Notification savedNotification = createNotification(notification);
        
        // Send email notification
        try {
            String emailSubject = "Thông báo giải đấu mới - " + tournamentName;
            String emailContent = String.format(
                "Xin chào %s,\n\n" +
                "Một giải đấu mới đã được tạo tại sân của bạn:\n" +
                "- Tên giải đấu: %s\n" +
                "- Sân: %s\n" +
                "- Ngày bắt đầu: %s\n\n" +
                "Vui lòng kiểm tra hệ thống để quản lý giải đấu.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ PlayerConnect",
                fieldOwner.getFullName(),
                tournamentName,
                fieldName,
                startDate
            );
            
            emailService.sendEmail(fieldOwner.getEmail(), emailSubject, emailContent);
        } catch (Exception e) {
            System.err.println("Failed to send tournament email notification: " + e.getMessage());
        }
        
        return savedNotification;
    }
    
    /**
     * Create notification for review request after booking completion
     */
    @Transactional
    public Notification createReviewNotificationForUser(User user, String fieldName, Long bookingId) {
        System.out.println("🔥 REVIEW NOTIFICATION DEBUG: Starting createReviewNotificationForUser");
        System.out.println("🔥 REVIEW NOTIFICATION DEBUG: User: " + user.getFullName() + ", Field: " + fieldName);
        
        try {
            // Create in-app notification
            Notification notification = new Notification();
            notification.setRecipient(user);
            notification.setTitle("Đánh giá sân");
            notification.setContent(String.format("Bạn đã hoàn thành trận đấu tại %s. Hãy chia sẻ trải nghiệm của bạn!", fieldName));
            notification.setType("REVIEW_REQUEST");
            notification.setRelatedEntityId(bookingId);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setIsRead(false);
            
            System.out.println("🔥 REVIEW NOTIFICATION DEBUG: About to call createNotification");
            Notification savedNotification = createNotification(notification);
            System.out.println("🔥 REVIEW NOTIFICATION DEBUG: Successfully created notification with ID: " + savedNotification.getId());
            
            return savedNotification;
        } catch (Exception e) {
            System.err.println("🔥 REVIEW NOTIFICATION DEBUG: CRITICAL ERROR in createReviewNotificationForUser: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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