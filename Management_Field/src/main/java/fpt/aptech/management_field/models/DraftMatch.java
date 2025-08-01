package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "draft_matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "creator_user_id")
    private User creator;
    
    @Column(name = "sport_type", columnDefinition = "NVARCHAR(MAX)")
    private String sportType;
    
    @Column(name = "location_description", columnDefinition = "NVARCHAR(MAX)")
    private String locationDescription;
    
    @Column(name = "estimated_start_time")
    private LocalDateTime estimatedStartTime;
    
    @Column(name = "estimated_end_time")
    private LocalDateTime estimatedEndTime;
    
    @Column(name = "slots_needed")
    private Integer slotsNeeded;
    
    @Column(name = "required_tags", columnDefinition = "NVARCHAR(MAX)")
    private String requiredTags; // JSON string storing list of required tags
    
    @Column(name = "skill_level", columnDefinition = "NVARCHAR(50)")
    private String skillLevel; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT, ANY
    
    @Column(name = "status", columnDefinition = "NVARCHAR(MAX)")
    private String status; // RECRUITING, FULL, AWAITING_CONFIRMATION, CONVERTED_TO_MATCH, CANCELLED
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "draft_match_interested_users",
        joinColumns = @JoinColumn(name = "draft_match_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> interestedUsers = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "RECRUITING";
        }
    }
    
    // Explicit getters and setters for compatibility
    public User getCreator() {
        return creator;
    }
    
    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Set<User> getInterestedUsers() {
        return interestedUsers;
    }
    
    public void setInterestedUsers(Set<User> interestedUsers) {
        this.interestedUsers = interestedUsers;
    }
    
    public String getSportType() {
        return sportType;
    }
    
    public void setSportType(String sportType) {
        this.sportType = sportType;
    }
    
    public String getLocationDescription() {
        return locationDescription;
    }
    
    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }
    
    public LocalDateTime getEstimatedStartTime() {
        return estimatedStartTime;
    }
    
    public void setEstimatedStartTime(LocalDateTime estimatedStartTime) {
        this.estimatedStartTime = estimatedStartTime;
    }
    
    public LocalDateTime getEstimatedEndTime() {
        return estimatedEndTime;
    }
    
    public void setEstimatedEndTime(LocalDateTime estimatedEndTime) {
        this.estimatedEndTime = estimatedEndTime;
    }
    
    public Integer getSlotsNeeded() {
        return slotsNeeded;
    }
    
    public void setSlotsNeeded(Integer slotsNeeded) {
        this.slotsNeeded = slotsNeeded;
    }
    
    public String getSkillLevel() {
        return skillLevel;
    }
    
    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRequiredTags() {
        return requiredTags;
    }
    
    public void setRequiredTags(String requiredTags) {
        this.requiredTags = requiredTags;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Check if the draft match is active (can accept new interests)
     */
    public boolean isActive() {
        return "RECRUITING".equals(status) || "FULL".equals(status);
    }
}