package fpt.aptech.management_field.payload.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long reviewId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long fieldId;
    private String fieldName;
    private Long bookingId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor for easy mapping
    public ReviewResponse(Long reviewId, Long userId, String userName, String userAvatar,
                         Long fieldId, String fieldName, Long bookingId, 
                         Integer rating, String comment, 
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.bookingId = bookingId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public ReviewResponse() {}
}