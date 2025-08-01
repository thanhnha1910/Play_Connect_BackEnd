package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String title;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String content;
    
    @Column(name = "type", columnDefinition = "NVARCHAR(MAX)")
    private String type; // e.g., "INVITE_RECEIVED", "REQUEST_ACCEPTED", "BOOKING_REMINDER"
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId; // e.g., the ID of the invitation or booking
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "is_read")
    private Boolean isRead;
    
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;
    
    // Explicit getters and setters for compatibility
    public String getType() {
        return type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getContent() {
        return content;
    }
    
    public Long getRelatedEntityId() {
        return relatedEntityId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public User getRecipient() {
        return recipient;
    }
    
    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}