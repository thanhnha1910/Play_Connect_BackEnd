package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "invitations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter; // The user who sends the invite/request
    
    @ManyToOne
    @JoinColumn(name = "invitee_id", nullable = false)
    private User invitee; // The user who receives the invite/request
    
    @ManyToOne
    @JoinColumn(name = "open_match_id", nullable = false)
    private OpenMatch openMatch; // The match this invitation is for
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InvitationType type; // INVITATION or REQUEST
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING; // PENDING, ACCEPTED, REJECTED
    
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
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Explicit getters for compatibility
    public User getInviter() {
        return inviter;
    }
    
    public User getInvitee() {
        return invitee;
    }
    
    public OpenMatch getOpenMatch() {
        return openMatch;
    }
    
    public InvitationType getType() {
        return type;
    }
    
    public Long getId() {
        return id;
    }
    
    public InvitationStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Explicit setters for compatibility
    public void setInviter(User inviter) {
        this.inviter = inviter;
    }
    
    public void setInvitee(User invitee) {
        this.invitee = invitee;
    }
    
    public void setOpenMatch(OpenMatch openMatch) {
        this.openMatch = openMatch;
    }
    
    public void setType(InvitationType type) {
        this.type = type;
    }
    
    public void setStatus(InvitationStatus status) {
        this.status = status;
    }
}