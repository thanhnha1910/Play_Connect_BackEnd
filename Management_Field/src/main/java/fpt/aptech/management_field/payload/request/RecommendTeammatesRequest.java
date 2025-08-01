package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;

public class RecommendTeammatesRequest {
    
    @NotBlank(message = "Sport type is required")
    private String sportType;
    
    @NotBlank(message = "Skill level is required")
    private String skillLevel;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @NotNull(message = "Number of teammates is required")
    @Min(value = 1, message = "Number of teammates must be at least 1")
    @Max(value = 20, message = "Number of teammates must not exceed 20")
    private Integer numberOfTeammates;
    
    private String preferredDateTime;
    
    private List<String> preferredPositions;
    
    private String ageRange;
    
    private String genderPreference;
    
    private Double maxDistance;
    
    private String notes;
    
    // Constructors
    public RecommendTeammatesRequest() {}
    
    public RecommendTeammatesRequest(String sportType, String skillLevel, String location, 
                                   Integer numberOfTeammates, String preferredDateTime, 
                                   List<String> preferredPositions, String ageRange, 
                                   String genderPreference, Double maxDistance, String notes) {
        this.sportType = sportType;
        this.skillLevel = skillLevel;
        this.location = location;
        this.numberOfTeammates = numberOfTeammates;
        this.preferredDateTime = preferredDateTime;
        this.preferredPositions = preferredPositions;
        this.ageRange = ageRange;
        this.genderPreference = genderPreference;
        this.maxDistance = maxDistance;
        this.notes = notes;
    }
    
    // Getters and Setters
    public String getSportType() {
        return sportType;
    }
    
    public void setSportType(String sportType) {
        this.sportType = sportType;
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
    
    public Integer getNumberOfTeammates() {
        return numberOfTeammates;
    }
    
    public void setNumberOfTeammates(Integer numberOfTeammates) {
        this.numberOfTeammates = numberOfTeammates;
    }
    
    public String getPreferredDateTime() {
        return preferredDateTime;
    }
    
    public void setPreferredDateTime(String preferredDateTime) {
        this.preferredDateTime = preferredDateTime;
    }
    
    public List<String> getPreferredPositions() {
        return preferredPositions;
    }
    
    public void setPreferredPositions(List<String> preferredPositions) {
        this.preferredPositions = preferredPositions;
    }
    
    public String getAgeRange() {
        return ageRange;
    }
    
    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
    }
    
    public String getGenderPreference() {
        return genderPreference;
    }
    
    public void setGenderPreference(String genderPreference) {
        this.genderPreference = genderPreference;
    }
    
    public Double getMaxDistance() {
        return maxDistance;
    }
    
    public void setMaxDistance(Double maxDistance) {
        this.maxDistance = maxDistance;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}