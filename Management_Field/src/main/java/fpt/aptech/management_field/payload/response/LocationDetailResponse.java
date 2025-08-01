package fpt.aptech.management_field.payload.response;

import fpt.aptech.management_field.payload.dtos.FieldTypeDto;
import fpt.aptech.management_field.payload.dtos.LocationReviewDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDetailResponse {
    private String name;
    private String address;
    private List<FieldTypeDto> fieldTypes;
    private List<LocationReviewDTO> reviews;
    
    // Explicit getters and setters for compatibility
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public List<FieldTypeDto> getFieldTypes() {
        return fieldTypes;
    }
    
    public void setFieldTypes(List<FieldTypeDto> fieldTypes) {
        this.fieldTypes = fieldTypes;
    }
    
    public List<LocationReviewDTO> getReviews() {
        return reviews;
    }
    
    public void setReviews(List<LocationReviewDTO> reviews) {
        this.reviews = reviews;
    }
}
