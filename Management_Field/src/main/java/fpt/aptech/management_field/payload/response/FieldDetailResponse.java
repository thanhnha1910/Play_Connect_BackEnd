package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldDetailResponse {
    private Long fieldId;
    private String description;
    private Integer hourlyRate;
    private LocalDateTime createdAt;
    private LocationInfo location;
    private TypeInfo type;
    private CategoryInfo category;
    private List<OperatingHourInfo> operatingHours;
    private ReviewSummary reviews;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo {
        private Long locationId;
        private String name;
        private String address;
        private String description;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Long ownerId;
        
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
        
        public void setOwnerId(Long ownerId) {
            this.ownerId = ownerId;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeInfo {
        private Long typeId;
        private String name;
        private Integer teamCapacity;
        private Integer maxCapacity;
        
        // Explicit setters for compatibility
        public void setTypeId(Long typeId) {
            this.typeId = typeId;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void setTeamCapacity(Integer teamCapacity) {
            this.teamCapacity = teamCapacity;
        }
        
        public void setMaxCapacity(Integer maxCapacity) {
            this.maxCapacity = maxCapacity;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long categoryId;
        private String name;
        private String description;
        
        // Explicit setters for compatibility
        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatingHourInfo {
        private Integer dayOfWeek;
        private String openingHour;
        private String closingHour;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewSummary {
        private BigDecimal averageRating;
        private Integer totalReviews;
    }
    
    // Explicit setters for compatibility
    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setLocation(LocationInfo location) {
        this.location = location;
    }
    
    public void setType(TypeInfo type) {
        this.type = type;
    }
    
    public void setCategory(CategoryInfo category) {
        this.category = category;
    }
}