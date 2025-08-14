package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenMatchDto {
    private Long id;
    private Long bookingId;
    private Long creatorUserId;
    private String creatorUserName;
    private String creatorAvatarUrl;
    private String sportType;
    private Integer slotsNeeded;
    private List<String> requiredTags;
    private String status;
    private LocalDateTime createdAt;
    private String fieldName;
    private String locationAddress;
    private Instant startTime;
    private Instant endTime;
    private LocalDate bookingDate;
    private Integer currentParticipants;
    private String locationName;
    private Double compatibilityScore; // For AI ranking
    private Double explicitScore; // AI explicit score
    private Double implicitScore; // AI implicit score
    private Double baseCompatibilityScore; // Base compatibility score for validation
    private Double originalAIScore; // Original AI score before validation
    private Boolean scoreValidated; // Whether score has been validated
    private Boolean aiScoreUsed; // Whether AI score was used instead of fallback
    private List<Long> participantIds;
    private String currentUserJoinStatus; // NOT_JOINED, REQUEST_PENDING, JOINED
    
    // Explicit getters and setters for compatibility
    public Long getId() {
        return id;
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
    
    public void setSlotsNeeded(Integer slotsNeeded) {
        this.slotsNeeded = slotsNeeded;
    }
    
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    
    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }
    
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
    
    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }
    
    public List<String> getRequiredTags() {
        return requiredTags;
    }
    
    public void setRequiredTags(List<String> requiredTags) {
        this.requiredTags = requiredTags;
    }
    
    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }
    
    public void setParticipantIds(List<Long> participantIds) {
        this.participantIds = participantIds;
    }
    
    public void setCurrentUserJoinStatus(String currentUserJoinStatus) {
        this.currentUserJoinStatus = currentUserJoinStatus;
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
    
    public void setBaseCompatibilityScore(Double baseCompatibilityScore) {
        this.baseCompatibilityScore = baseCompatibilityScore;
    }
    
    public void setOriginalAIScore(Double originalAIScore) {
        this.originalAIScore = originalAIScore;
    }
    
    public void setScoreValidated(Boolean scoreValidated) {
        this.scoreValidated = scoreValidated;
    }
    
    public Boolean getAiScoreUsed() {
        return aiScoreUsed;
    }
    
    public void setAiScoreUsed(Boolean aiScoreUsed) {
        this.aiScoreUsed = aiScoreUsed;
    }
}