package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.*;
import fpt.aptech.management_field.payload.request.ReviewRequest;
import fpt.aptech.management_field.payload.response.ReviewResponse;
import fpt.aptech.management_field.payload.response.FieldRatingResponse;
import fpt.aptech.management_field.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final FieldRepository fieldRepository;
    private final NotificationService notificationService;
    private final LocationReviewService locationReviewService;
    
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        // Validate booking exists and belongs to user
        Booking booking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new RuntimeException("Booking not found"));
            
        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only review your own bookings");
        }
        
        // Check if booking is completed
        if (!"COMPLETED".equals(booking.getStatus()) && 
            booking.getToTime().isAfter(java.time.Instant.now())) {
            throw new RuntimeException("You can only review completed bookings");
        }
        
        // Check if already reviewed
        if (reviewRepository.existsByBooking_BookingId(request.getBookingId())) {
            throw new RuntimeException("You have already reviewed this booking");
        }
        
        // Create review
        Review review = new Review();
        review.setUser(booking.getUser());
        review.setField(booking.getField());
        review.setBooking(booking);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());
        
        Review savedReview = reviewRepository.save(review);
        
        // Send notification to field owner
        String message = String.format("New review received for %s - %d stars", 
            booking.getField().getName(), request.getRating());
        
        Notification notification = new Notification();
        notification.setRecipient(booking.getField().getLocation().getOwner().getUser());
        notification.setType("NEW_REVIEW");
        notification.setTitle("New Review");
        notification.setContent(message);
        notification.setRelatedEntityId(savedReview.getReviewId());
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationService.createNotification(notification);
        
        // Tự động cập nhật location review
        locationReviewService.onNewFieldReview(booking.getField().getFieldId());
        
        return mapToResponse(savedReview);
    }
    
    public List<ReviewResponse> getReviewsByField(Long fieldId) {
        List<Review> reviews = reviewRepository.findByFieldIdOrderByCreatedAtDesc(fieldId);
        return reviews.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
    public List<ReviewResponse> getReviewsByUser(Long userId) {
        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return reviews.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
    public FieldRatingResponse getFieldRating(Long fieldId) {
        Field field = fieldRepository.findById(fieldId)
            .orElseThrow(() -> new RuntimeException("Field not found"));
            
        Double averageRating = reviewRepository.getAverageRatingByFieldId(fieldId);
        Long totalReviews = reviewRepository.countReviewsByFieldId(fieldId);
        
        // Get recent reviews (limit 5)
        Pageable pageable = PageRequest.of(0, 5);
        List<Review> recentReviews = reviewRepository.findRecentReviewsByFieldId(fieldId, pageable);
        
        FieldRatingResponse response = new FieldRatingResponse(
            fieldId, field.getName(), averageRating, totalReviews
        );
        
        response.setRecentReviews(
            recentReviews.stream().map(this::mapToResponse).collect(Collectors.toList())
        );
        
        return response;
    }
    
    public boolean canUserReviewBooking(Long userId, Long bookingId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            return false;
        }
        
        Booking b = booking.get();
        
        // Check if booking belongs to user
        if (!b.getUser().getId().equals(userId)) {
            return false;
        }
        
        // Check if booking is completed or time has passed
        if (!"COMPLETED".equals(b.getStatus()) && 
            b.getToTime().isAfter(java.time.Instant.now())) {
            return false;
        }
        
        // Check if not already reviewed
        return !reviewRepository.existsByBooking_BookingId(bookingId);
    }
    
    public List<ReviewResponse> getOwnerFieldReviews(Long ownerId) {
        List<Review> reviews = reviewRepository.findReviewsForOwnerFields(ownerId);
        
        // Debug logs để kiểm tra location information
        System.out.println("=== OWNER FIELD REVIEWS DEBUG ===");
        System.out.println("Owner ID: " + ownerId);
        System.out.println("Total reviews found: " + reviews.size());
        
        for (int i = 0; i < reviews.size(); i++) {
            Review review = reviews.get(i);
            System.out.println("Review " + (i + 1) + ":");
            System.out.println("  - Review ID: " + review.getReviewId());
            System.out.println("  - Field ID: " + review.getField().getFieldId());
            System.out.println("  - Field Name: " + review.getField().getName());
            System.out.println("  - Location ID: " + review.getField().getLocation().getLocationId());
            System.out.println("  - Location Name: " + review.getField().getLocation().getName());
            System.out.println("  - User: " + review.getUser().getFullName());
            System.out.println("  - Rating: " + review.getRating());
        }
        
        List<ReviewResponse> responses = reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
                
        // Debug unique locations
        long uniqueLocations = responses.stream()
                .map(ReviewResponse::getLocationName)
                .distinct()
                .count();
        System.out.println("Unique locations in responses: " + uniqueLocations);
        responses.stream()
                .map(ReviewResponse::getLocationName)
                .distinct()
                .forEach(locationName -> System.out.println("  - " + locationName));
        System.out.println("=== END OWNER FIELD REVIEWS DEBUG ===");
        
        return responses;
    }
    
    public List<ReviewResponse> getReviewsByLocation(Long locationId) {
        List<Review> reviews = reviewRepository.findByFieldLocationId(locationId);
        return reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ReviewResponse replyToReview(Long reviewId, Long ownerId, String reply) {
        // Find the review
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Validate that the owner owns the field being reviewed
        if (!review.getField().getLocation().getOwner().getUser().getId().equals(ownerId)) {
            throw new RuntimeException("You can only reply to reviews of your own fields");
        }
        
        // Update the review with owner reply
        review.setOwnerReply(reply);
        review.setOwnerReplyAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        
        Review savedReview = reviewRepository.save(review);
        
        // Send notification to the reviewer
        String message = String.format("Owner replied to your review for %s", 
            review.getField().getName());
        
        Notification notification = new Notification();
        notification.setRecipient(review.getUser());
        notification.setType("OWNER_REPLY");
        notification.setTitle("Owner Reply");
        notification.setContent(message);
        notification.setRelatedEntityId(savedReview.getReviewId());
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationService.createNotification(notification);
        
        return mapToResponse(savedReview);
    }
    
    private ReviewResponse mapToResponse(Review review) {
        return new ReviewResponse(
            review.getReviewId(),
            review.getUser().getId(),
            review.getUser().getFullName(),
            review.getUser().getProfilePicture(),
            review.getField().getFieldId(),
            review.getField().getName(),
            review.getField().getLocation().getLocationId(),
            review.getField().getLocation().getName(),
            review.getBooking().getBookingId(),
            review.getRating(),
            review.getComment(),
            review.getOwnerReply(),
            review.getOwnerReplyAt(),
            review.getCreatedAt(),
            review.getUpdatedAt()
        );
    }
}