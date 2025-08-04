package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldSummaryResponse {
    private Long fieldId;
    private String fieldName;
    private Integer hourlyRate;
    private String typeName;
    private String categoryName;
    private String thumbnailImageUrl;
    private String imageGallery;
    
    // Explicit setters for compatibility
    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public void setHourlyRate(Integer hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
    
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public void setThumbnailImageUrl(String thumbnailImageUrl) {
        this.thumbnailImageUrl = thumbnailImageUrl;
    }
    
    public void setImageGallery(String imageGallery) {
        this.imageGallery = imageGallery;
    }
}