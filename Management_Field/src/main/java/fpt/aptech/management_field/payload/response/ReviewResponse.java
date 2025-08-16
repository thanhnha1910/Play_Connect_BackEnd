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
    private Long locationId;
    private String locationName;
    private Long bookingId;
    private Integer rating;
    private String comment;
    private String ownerReply;
    private LocalDateTime ownerReplyAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor for easy mapping
    public ReviewResponse(Long reviewId, Long userId, String userName, String userAvatar,
                         Long fieldId, String fieldName, Long locationId, String locationName,
                         Long bookingId, Integer rating, String comment, String ownerReply, 
                         LocalDateTime ownerReplyAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.locationId = locationId;
        this.locationName = locationName;
        this.bookingId = bookingId;
        this.rating = rating;
        this.comment = comment;
        this.ownerReply = ownerReply;
        this.ownerReplyAt = ownerReplyAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public ReviewResponse() {}
}