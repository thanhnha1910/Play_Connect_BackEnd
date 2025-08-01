package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftMatchDto {
    private Long id;
    private Long creatorUserId;
    private String creatorUserName;
    private String creatorAvatarUrl;
    private String sportType;
    private String locationDescription;
    private LocalDateTime estimatedStartTime;
    private LocalDateTime estimatedEndTime;
    private Integer slotsNeeded;
    private String skillLevel;
    private List<String> requiredTags;
    private String status;
    private LocalDateTime createdAt;
    private Integer interestedUsersCount;
    private List<Long> interestedUserIds;
    private Integer pendingUsersCount;
    private Integer approvedUsersCount;
    private List<UserStatusDto> userStatuses;
    private Boolean currentUserInterested;
    private String currentUserStatus;
    private Double compatibilityScore; // AI compatibility score
    private Double explicitScore; // AI explicit score
    private Double implicitScore; // AI implicit score
    
    // Explicit getters and setters for compatibility
    public Long getId() {
        return id;
    }
    
    public Double getCompatibilityScore() {
        return compatibilityScore;
    }
    
    public void setCompatibilityScore(Double compatibilityScore) {
        this.compatibilityScore = compatibilityScore;
    }
    
    public Double getExplicitScore() {
        return explicitScore;
    }
    
    public void setExplicitScore(Double explicitScore) {
        this.explicitScore = explicitScore;
    }
    
    public Double getImplicitScore() {
        return implicitScore;
    }
    
    public void setImplicitScore(Double implicitScore) {
        this.implicitScore = implicitScore;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setCreatorUserId(Long creatorUserId) {
        this.creatorUserId = creatorUserId;
    }
    
    public void setCreatorUserName(String creatorUserName) {
        this.creatorUserName = creatorUserName;
    }
    
    public void setCreatorAvatarUrl(String creatorAvatarUrl) {
        this.creatorAvatarUrl = creatorAvatarUrl;
    }
    
    public void setSportType(String sportType) {
        this.sportType = sportType;
    }
    
    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }
    
    public void setEstimatedStartTime(LocalDateTime estimatedStartTime) {
        this.estimatedStartTime = estimatedStartTime;
    }
    
    public void setEstimatedEndTime(LocalDateTime estimatedEndTime) {
        this.estimatedEndTime = estimatedEndTime;
    }
    
    public void setSlotsNeeded(Integer slotsNeeded) {
        this.slotsNeeded = slotsNeeded;
    }
    
    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setRequiredTags(List<String> requiredTags) {
        this.requiredTags = requiredTags;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setInterestedUsersCount(Integer interestedUsersCount) {
        this.interestedUsersCount = interestedUsersCount;
    }
    
    public void setInterestedUserIds(List<Long> interestedUserIds) {
        this.interestedUserIds = interestedUserIds;
    }
    
    public void setPendingUsersCount(Integer pendingUsersCount) {
        this.pendingUsersCount = pendingUsersCount;
    }
    
    public void setApprovedUsersCount(Integer approvedUsersCount) {
        this.approvedUsersCount = approvedUsersCount;
    }
    
    public void setUserStatuses(List<UserStatusDto> userStatuses) {
        this.userStatuses = userStatuses;
    }
    
    public void setCurrentUserInterested(Boolean currentUserInterested) {
        this.currentUserInterested = currentUserInterested;
    }
    
    public void setCurrentUserStatus(String currentUserStatus) {
        this.currentUserStatus = currentUserStatus;
    }
}