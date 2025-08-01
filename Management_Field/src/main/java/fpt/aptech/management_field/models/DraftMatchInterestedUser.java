package fpt.aptech.management_field.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing the relationship between a Draft Match and interested users
 * This is a join table that tracks which users are interested in which draft matches
 * and their current status (pending, accepted, rejected)
 */
@Entity
@Table(name = "draft_match_interested_users", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"draft_match_id", "user_id"})
       })
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DraftMatchInterestedUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_match_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DraftMatch draftMatch;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InterestStatus status = InterestStatus.PENDING;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "message", length = 500)
    private String message; // Optional message from user when expressing interest
    
    // AI compatibility scores for this specific user-draft match combination
    @Column(name = "ai_compatibility_score")
    private Double aiCompatibilityScore;
    
    @Column(name = "ai_explicit_score")
    private Double aiExplicitScore;
    
    @Column(name = "ai_implicit_score")
    private Double aiImplicitScore;
    
    @Column(name = "ai_last_updated")
    private LocalDateTime aiLastUpdated;
    
    // Constructors
    public DraftMatchInterestedUser() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = InterestStatus.PENDING;
    }
    
    public DraftMatchInterestedUser(DraftMatch draftMatch, User user) {
        this();
        this.draftMatch = draftMatch;
        this.user = user;
    }
    
    public DraftMatchInterestedUser(DraftMatch draftMatch, User user, String message) {
        this(draftMatch, user);
        this.message = message;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = InterestStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public DraftMatch getDraftMatch() {
        return draftMatch;
    }
    
    public void setDraftMatch(DraftMatch draftMatch) {
        this.draftMatch = draftMatch;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public InterestStatus getStatus() {
        return status;
    }
    
    public void setStatus(InterestStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Double getAiCompatibilityScore() {
        return aiCompatibilityScore;
    }
    
    public void setAiCompatibilityScore(Double aiCompatibilityScore) {
        this.aiCompatibilityScore = aiCompatibilityScore;
    }
    
    public Double getAiExplicitScore() {
        return aiExplicitScore;
    }
    
    public void setAiExplicitScore(Double aiExplicitScore) {
        this.aiExplicitScore = aiExplicitScore;
    }
    
    public Double getAiImplicitScore() {
        return aiImplicitScore;
    }
    
    public void setAiImplicitScore(Double aiImplicitScore) {
        this.aiImplicitScore = aiImplicitScore;
    }
    
    public LocalDateTime getAiLastUpdated() {
        return aiLastUpdated;
    }
    
    public void setAiLastUpdated(LocalDateTime aiLastUpdated) {
        this.aiLastUpdated = aiLastUpdated;
    }
    
    // Helper methods
    public boolean isPending() {
        return this.status == InterestStatus.PENDING;
    }
    
    public boolean isAccepted() {
        return this.status == InterestStatus.ACCEPTED;
    }
    
    public boolean isRejected() {
        return this.status == InterestStatus.REJECTED;
    }
    
    public boolean hasAiScores() {
        return aiCompatibilityScore != null || aiExplicitScore != null || aiImplicitScore != null;
    }
    
    public void updateAiScores(Double compatibilityScore, Double explicitScore, Double implicitScore) {
        this.aiCompatibilityScore = compatibilityScore;
        this.aiExplicitScore = explicitScore;
        this.aiImplicitScore = implicitScore;
        this.aiLastUpdated = LocalDateTime.now();
    }
    
    public void clearAiScores() {
        this.aiCompatibilityScore = null;
        this.aiExplicitScore = null;
        this.aiImplicitScore = null;
        this.aiLastUpdated = null;
    }
    
    public void accept() {
        this.status = InterestStatus.ACCEPTED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void reject() {
        this.status = InterestStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void resetToPending() {
        this.status = InterestStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business logic methods
    public boolean canBeAccepted() {
        return this.status == InterestStatus.PENDING && 
               this.draftMatch != null && 
               this.draftMatch.isActive();
    }
    
    public boolean canBeRejected() {
        return (this.status == InterestStatus.PENDING || this.status == InterestStatus.ACCEPTED) && 
               this.draftMatch != null && 
               this.draftMatch.isActive();
    }
    
    public boolean canWithdraw() {
        return this.status == InterestStatus.PENDING && 
               this.draftMatch != null && 
               this.draftMatch.isActive();
    }
    
    // toString method
    @Override
    public String toString() {
        return "DraftMatchInterestedUser{" +
                "id=" + id +
                ", draftMatchId=" + (draftMatch != null ? draftMatch.getId() : null) +
                ", userId=" + (user != null ? user.getId() : null) +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
    
    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DraftMatchInterestedUser)) return false;
        
        DraftMatchInterestedUser that = (DraftMatchInterestedUser) o;
        
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        
        // If IDs are null, compare by draft match and user
        return draftMatch != null && draftMatch.equals(that.draftMatch) &&
               user != null && user.equals(that.user);
    }
    
    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        
        int result = draftMatch != null ? draftMatch.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}