package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.payload.request.ReviewRequest;
import fpt.aptech.management_field.payload.response.ReviewResponse;
import fpt.aptech.management_field.payload.response.FieldRatingResponse;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.services.ReviewService;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewRequest request,
                                         Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            ReviewResponse review = reviewService.createReview(userDetails.getId(), request);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/field/{fieldId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByField(@PathVariable Long fieldId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByField(fieldId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/field/{fieldId}/rating")
    public ResponseEntity<FieldRatingResponse> getFieldRating(@PathVariable Long fieldId) {
        FieldRatingResponse rating = reviewService.getFieldRating(fieldId);
        return ResponseEntity.ok(rating);
    }
    
    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<ReviewResponse> reviews = reviewService.getReviewsByUser(userDetails.getId());
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/can-review/{bookingId}")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER')")
    public ResponseEntity<Boolean> canReviewBooking(@PathVariable Long bookingId,
                                                   Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean canReview = reviewService.canUserReviewBooking(userDetails.getId(), bookingId);
        return ResponseEntity.ok(canReview);
    }
    
    @GetMapping("/owner/reviews")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ReviewResponse>> getOwnerFieldReviews(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<ReviewResponse> reviews = reviewService.getOwnerFieldReviews(userDetails.getId());
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByLocation(@PathVariable Long locationId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByLocation(locationId);
        return ResponseEntity.ok(reviews);
    }
    
    @PostMapping("/{reviewId}/reply")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> replyToReview(@PathVariable Long reviewId,
                                          @RequestBody String reply,
                                          Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            ReviewResponse updatedReview = reviewService.replyToReview(reviewId, userDetails.getId(), reply);
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}