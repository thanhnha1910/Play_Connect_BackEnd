package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {
    private Long locationId;
    private String name;
    private String address;
    private String city;     // Thêm field này
    private String country;  // Thêm field này
    private String thumbnailUrl; // Thêm field cho thumbnail
    private Integer fieldCount; // Thêm field đếm số sân
    private Integer fieldTypeCount; // Thêm field đếm số loại sân

    // Explicit setters for compatibility
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    // Thêm các setter mới
    public void setCity(String city) {
        this.city = city;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public void setFieldCount(Integer fieldCount) {
        this.fieldCount = fieldCount;
    }
    
    public void setFieldTypeCount(Integer fieldTypeCount) {
        this.fieldTypeCount = fieldTypeCount;
    }
}
