package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class BookingRequest {
    @NotNull(message = "Field ID is required")
    private Long fieldId;

    @NotNull(message = "From time is required")
    private Instant fromTime;

    @NotNull(message = "To time is required")
    private Instant toTime;

    private Integer slots;

    private boolean findTeammates;

    private List<AdditionalPlayer> additionalPlayers;

    @Data
    public static class AdditionalPlayer {
        @NotNull(message = "User ID is required")
        private Long userId;

        private String position;
        
        // Explicit getters and setters for compatibility
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getPosition() {
            return position;
        }
        
        public void setPosition(String position) {
            this.position = position;
        }
    }
    
    // Explicit getters and setters for compatibility
    public Long getFieldId() {
        return fieldId;
    }
    
    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
    
    public Instant getFromTime() {
        return fromTime;
    }
    
    public void setFromTime(Instant fromTime) {
        this.fromTime = fromTime;
    }
    
    public Instant getToTime() {
        return toTime;
    }
    
    public void setToTime(Instant toTime) {
        this.toTime = toTime;
    }
    
    public Integer getSlots() {
        return slots;
    }
    
    public void setSlots(Integer slots) {
        this.slots = slots;
    }
    
    public boolean isFindTeammates() {
        return findTeammates;
    }
    
    public void setFindTeammates(boolean findTeammates) {
        this.findTeammates = findTeammates;
    }
    
    public List<AdditionalPlayer> getAdditionalPlayers() {
        return additionalPlayers;
    }
    
    public void setAdditionalPlayers(List<AdditionalPlayer> additionalPlayers) {
        this.additionalPlayers = additionalPlayers;
    }
}