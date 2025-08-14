package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationCardResponse {
    private Long locationId;
    private String locationName;
    private String slug;
    private String address;
    private String description;
    private String mainImageUrl;
    private int fieldCount;
    private Double averageRating;
    private BigDecimal startingPrice;
    private Long bookingCount;
    
    // Explicit setters for compatibility
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
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
    
    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }
    
    public void setFieldCount(int fieldCount) {
        this.fieldCount = fieldCount;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }
    
    public void setBookingCount(Long bookingCount) {
        this.bookingCount = bookingCount;
    }
}