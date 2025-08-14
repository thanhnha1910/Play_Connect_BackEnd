package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationMapResponse {
    private Long locationId;
    private String name;
    private String slug;
    private String address;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer fieldCount;
    private BigDecimal averageRating;
    private String thumbnailImageUrl;
    private BigDecimal startingPrice;
    
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
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
    
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public void setFieldCount(Integer fieldCount) {
        this.fieldCount = fieldCount;
    }
    
    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }
    
    public void setThumbnailImageUrl(String thumbnailImageUrl) {
        this.thumbnailImageUrl = thumbnailImageUrl;
    }
    
    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }
}