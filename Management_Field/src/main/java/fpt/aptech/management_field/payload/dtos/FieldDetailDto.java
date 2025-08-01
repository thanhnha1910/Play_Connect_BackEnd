package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldDetailDto {
    private Long fieldId;
    private String name;
    private String description;
    private String fieldTypeName;
    private String locationName;
    private Boolean isActive;
    private Integer hourlyRate;
    private Long typeId;
    private Long locationId;
    private String thumbnailUrl;
    private String imageGallery;
    
    // Explicit setters for compatibility
    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }
    
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public void setImageGallery(String imageGallery) {
        this.imageGallery = imageGallery;
    }
}