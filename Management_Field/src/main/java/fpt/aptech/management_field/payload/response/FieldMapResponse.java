package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldMapResponse {
    private Long fieldId;
    private String fieldName;
    private String locationName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String thumbnailImageUrl;
    private String addressShort;
    private BigDecimal averageRating;
    private Integer hourlyRate;
    private Long typeId;
    private String typeName;
    private Long categoryId;
    private String categoryName;
    
    // Explicit setters for compatibility
    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
    
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
    
    public void setAddressShort(String addressShort) {
        this.addressShort = addressShort;
    }
    
    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }
    
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}