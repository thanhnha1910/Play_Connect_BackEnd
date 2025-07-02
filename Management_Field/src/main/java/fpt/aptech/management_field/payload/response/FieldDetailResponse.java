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
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Long ownerId;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeInfo {
        private Long typeId;
        private String name;
        private Integer teamCapacity;
        private Integer maxCapacity;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long categoryId;
        private String name;
        private String description;
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
}