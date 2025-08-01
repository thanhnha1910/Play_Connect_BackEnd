package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "draft_match_user_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftMatchUserStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "draft_match_id")
    private DraftMatch draftMatch;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "status", columnDefinition = "NVARCHAR(50)")
    private String status; // PENDING, APPROVED, REJECTED
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Explicit setters for compatibility
    public void setDraftMatch(DraftMatch draftMatch) {
        this.draftMatch = draftMatch;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public User getUser() {
        return user;
    }
    
    public String getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public DraftMatch getDraftMatch() {
        return draftMatch;
    }
}