package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.Notification;
import fpt.aptech.management_field.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
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
}