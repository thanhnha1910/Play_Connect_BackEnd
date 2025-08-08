package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldTypeDto {
    private Long typeId;
    private String name;
    private Integer teamCapacity;
    private Integer maxCapacity;
    private Integer hourlyRate;
    private String description;
    private Long locationId;
    private String locationName;
    private List<FieldDTO> fields;
    
    // Constructor for LocationService usage
    public FieldTypeDto(String name, Integer teamCapacity, Integer maxCapacity, List<FieldDTO> fields) {
        this.name = name;
        this.teamCapacity = teamCapacity;
        this.maxCapacity = maxCapacity;
        this.fields = fields;
    }
    
    // Explicit setters for compatibility
    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setTeamCapacity(Integer teamCapacity) {
        this.teamCapacity = teamCapacity;
    }
    
    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
    
    public void setHourlyRate(Integer hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    
    public void setFields(List<FieldDTO> fields) {
        this.fields = fields;
    }
}