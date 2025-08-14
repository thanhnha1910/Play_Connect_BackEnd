package fpt.aptech.management_field.payload.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LocationResponse {
    private Long locationId;
    private String name;
    private String slug;
    private String address;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String thumbnailUrl;
    private String imageGallery;
    private Long ownerId;
    
    // Explicit setters for compatibility
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
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
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public void setImageGallery(String imageGallery) {
        this.imageGallery = imageGallery;
    }
    
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}