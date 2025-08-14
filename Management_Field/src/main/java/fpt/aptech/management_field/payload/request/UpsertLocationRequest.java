package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpsertLocationRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    private String description;
    
    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;
    
    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;
    
    private String thumbnailUrl;
    
    private String imageGallery; // JSON string of URLs
    
    // Explicit getters for compatibility
    public String getName() {
        return name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getDescription() {
        return description;
    }
    
    public BigDecimal getLatitude() {
        return latitude;
    }
    
    public BigDecimal getLongitude() {
        return longitude;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public String getImageGallery() {
        return imageGallery;
    }
}