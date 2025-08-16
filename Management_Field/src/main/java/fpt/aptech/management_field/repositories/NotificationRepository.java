package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByRecipientId(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND n.isRead = true ORDER BY n.createdAt DESC")
    List<Notification> findReadByRecipientId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :userId AND n.isRead = false")
    Long countUnreadByRecipientId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.type = :notificationType AND n.relatedEntityId = :entityId")
    Long findNotificationCountByTypeAndEntityId(@Param("notificationType") String type, @Param("entityId") Long relatedEntityId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.type = :notificationType AND n.relatedEntityId = :entityId AND n.recipient.id = :recipientId")
    Long findNotificationCountByTypeEntityIdAndRecipient(@Param("notificationType") String type, @Param("entityId") Long relatedEntityId, @Param("recipientId") Long recipientId);
}