package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.LocationReview;
import fpt.aptech.management_field.payload.dtos.LocationReviewDTO;
import fpt.aptech.management_field.payload.response.FieldRatingResponse;
import fpt.aptech.management_field.services.LocationReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location-reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LocationReviewController {
    
    private final LocationReviewService locationReviewService;
    
    /**
     * Lấy tất cả location reviews cho một location
     */
    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<LocationReviewDTO>> getLocationReviews(@PathVariable Long locationId) {
        try {
            List<LocationReviewDTO> reviews = locationReviewService.getLocationReviewDTOs(locationId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Lấy thống kê rating của location
     */
    @GetMapping("/location/{locationId}/rating")
    public ResponseEntity<FieldRatingResponse> getLocationRating(@PathVariable Long locationId) {
        try {
            FieldRatingResponse rating = locationReviewService.getLocationRatingStats(locationId);
            return ResponseEntity.ok(rating);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Tính toán lại location review cho một location cụ thể
     */
    @PostMapping("/recalculate/{locationId}")
    public ResponseEntity<String> recalculateLocationReview(@PathVariable Long locationId) {
        try {
            locationReviewService.updateLocationReviewFromFieldReviews(locationId);
            return ResponseEntity.ok("Location review updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Tính toán lại tất cả location reviews (admin only)
     */
    @PostMapping("/recalculate-all")
    public ResponseEntity<String> recalculateAllLocationReviews() {
        try {
            locationReviewService.recalculateAllLocationReviews();
            return ResponseEntity.ok("All location reviews updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}