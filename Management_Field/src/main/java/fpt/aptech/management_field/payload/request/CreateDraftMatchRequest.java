package fpt.aptech.management_field.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDraftMatchRequest {
    private String sportType;
    private String locationDescription;
    private LocalDateTime estimatedStartTime;
    private LocalDateTime estimatedEndTime;
    private Integer slotsNeeded;
    private String skillLevel;
    private List<String> requiredTags;
    
    // Explicit getters for compatibility
    public String getSportType() {
        return sportType;
    }
    
    public String getLocationDescription() {
        return locationDescription;
    }
    
    public LocalDateTime getEstimatedStartTime() {
        return estimatedStartTime;
    }
    
    public LocalDateTime getEstimatedEndTime() {
        return estimatedEndTime;
    }
    
    public Integer getSlotsNeeded() {
        return slotsNeeded;
    }
    
    public String getSkillLevel() {
        return skillLevel;
    }
    
    public List<String> getRequiredTags() {
        return requiredTags;
    }
}