package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "open_matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
    @ManyToOne
    @JoinColumn(name = "creator_user_id")
    private User creatorUser;
    
    @Column(name = "sport_type", columnDefinition = "NVARCHAR(MAX)")
    private String sportType;
    
    @Column(name = "slots_needed")
    private Integer slotsNeeded;
    
    @Column(name = "required_tags", columnDefinition = "NVARCHAR(MAX)")
    private String requiredTags; // JSON string storing list of required tags
    
    @Column(name = "status", columnDefinition = "NVARCHAR(MAX)")
    private String status; // OPEN, FULL, CLOSED
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "OPEN";
        }
    }
    
    // Explicit getters for compatibility
    public Long getId() {
        return id;
    }
    
    public String getSportType() {
        return sportType;
    }
    
    public Integer getSlotsNeeded() {
        return slotsNeeded;
    }
    
    public String getStatus() {
        return status;
    }
    
    public Booking getBooking() {
        return booking;
    }
    
    public User getCreatorUser() {
        return creatorUser;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public String getRequiredTags() {
        return requiredTags;
    }
    
    // Explicit setter for compatibility
    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    
    public void setCreatorUser(User creatorUser) {
        this.creatorUser = creatorUser;
    }
    
    public void setRequiredTags(String requiredTags) {
        this.requiredTags = requiredTags;
    }
    
    public void setSportType(String sportType) {
        this.sportType = sportType;
    }
    
    public void setSlotsNeeded(Integer slotsNeeded) {
        this.slotsNeeded = slotsNeeded;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}