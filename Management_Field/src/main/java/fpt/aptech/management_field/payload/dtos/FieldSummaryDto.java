package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldSummaryDto {
    private Long fieldId;
    private String name;
    private String fieldTypeName;
    private String locationName;
    private Boolean isActive;
    private Integer hourlyRate;
    
    // Explicit setters for compatibility
    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setFieldTypeName(String fieldTypeName) {
        this.fieldTypeName = fieldTypeName;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public void setHourlyRate(Integer hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
}