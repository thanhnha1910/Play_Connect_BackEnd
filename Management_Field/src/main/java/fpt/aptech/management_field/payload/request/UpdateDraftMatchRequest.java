package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

public class UpdateDraftMatchRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotBlank(message = "Sport type is required")
    private String sportType;
    
    @NotNull(message = "Max players is required")
    @Min(value = 2, message = "Max players must be at least 2")
    @Max(value = 50, message = "Max players must not exceed 50")
    private Integer maxPlayers;
    
    @NotNull(message = "Slots needed is required")
    @Min(value = 1, message = "Slots needed must be at least 1")
    private Integer slotsNeeded;
    
    @NotBlank(message = "Skill level is required")
    private String skillLevel;
    
    @NotBlank(message = "Location is required")
    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;
    
    private String preferredDateTime;
    
    private String notes;
    
    // Constructors
    public UpdateDraftMatchRequest() {}
    
    public UpdateDraftMatchRequest(String title, String description, String sportType, 
                                 Integer maxPlayers, Integer slotsNeeded, String skillLevel, 
                                 String location, String preferredDateTime, String notes) {
        this.title = title;
        this.description = description;
        this.sportType = sportType;
        this.maxPlayers = maxPlayers;
        this.slotsNeeded = slotsNeeded;
        this.skillLevel = skillLevel;
        this.location = location;
        this.preferredDateTime = preferredDateTime;
        this.notes = notes;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSportType() {
        return sportType;
    }
    
    public void setSportType(String sportType) {
        this.sportType = sportType;
    }
    
    public Integer getMaxPlayers() {
        return maxPlayers;
    }
    
    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
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
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getPreferredDateTime() {
        return preferredDateTime;
    }
    
    public void setPreferredDateTime(String preferredDateTime) {
        this.preferredDateTime = preferredDateTime;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}