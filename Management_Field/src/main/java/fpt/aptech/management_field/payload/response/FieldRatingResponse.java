package fpt.aptech.management_field.payload.response;

import lombok.Data;
import java.util.List;

@Data
public class FieldRatingResponse {
    private Long fieldId;
    private String fieldName;
    private Double averageRating;
    private Long totalReviews;
    private List<ReviewResponse> recentReviews;
    
    // Rating distribution (1-5 stars)
    private Long oneStar;
    private Long twoStar;
    private Long threeStar;
    private Long fourStar;
    private Long fiveStar;
    
    // Setter methods for rating counts
    public void setRating1Count(Long count) {
        this.oneStar = count;
    }
    
    public void setRating2Count(Long count) {
        this.twoStar = count;
    }
    
    public void setRating3Count(Long count) {
        this.threeStar = count;
    }
    
    public void setRating4Count(Long count) {
        this.fourStar = count;
    }
    
    public void setRating5Count(Long count) {
        this.fiveStar = count;
    }
    
    public FieldRatingResponse() {}
    
    public FieldRatingResponse(Long fieldId, String fieldName, Double averageRating, Long totalReviews) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }
}