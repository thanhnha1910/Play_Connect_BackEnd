package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.*;
import fpt.aptech.management_field.payload.dtos.LocationReviewDTO;
import fpt.aptech.management_field.payload.dtos.UserDto;
import fpt.aptech.management_field.payload.response.FieldRatingResponse;
import fpt.aptech.management_field.repositories.*;
import fpt.aptech.management_field.mappers.LocationReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationReviewService {
    
    private final LocationReviewRepository locationReviewRepository;
    private final ReviewRepository reviewRepository;
    private final LocationRepository locationRepository;
    private final FieldRepository fieldRepository;
    private final LocationReviewMapper locationReviewMapper;
    
    /**
     * Tự động cập nhật hoặc tạo location review dựa trên các review của các sân trong location
     * @param locationId ID của location cần cập nhật
     */
    @Transactional
    public void updateLocationReviewFromFieldReviews(Long locationId) {
        System.out.println("🔥 DEBUG: updateLocationReviewFromFieldReviews called for locationId: " + locationId);
        Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new RuntimeException("Location not found"));
        
        // Get all reviews for fields in this location
        List<Review> allFieldReviews = reviewRepository.findByFieldLocationId(locationId);
        System.out.println("🔥 DEBUG: Found " + allFieldReviews.size() + " reviews for location " + locationId);
        
        if (allFieldReviews.isEmpty()) {
            // No reviews yet, delete location review if exists
            System.out.println("🔥 DEBUG: No reviews found, deleting location review if exists");
            locationReviewRepository.deleteByLocationId(locationId);
            return;
        }
        
        // Calculate statistics
        double averageRating = allFieldReviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
        
        long totalReviews = allFieldReviews.size();
        System.out.println("🔥 DEBUG: Calculated averageRating: " + averageRating + ", totalReviews: " + totalReviews);
        
        // Count ratings by star
        long rating1Count = allFieldReviews.stream().filter(r -> r.getRating() == 1).count();
        long rating2Count = allFieldReviews.stream().filter(r -> r.getRating() == 2).count();
        long rating3Count = allFieldReviews.stream().filter(r -> r.getRating() == 3).count();
        long rating4Count = allFieldReviews.stream().filter(r -> r.getRating() == 4).count();
        long rating5Count = allFieldReviews.stream().filter(r -> r.getRating() == 5).count();
        
        // Find or create location review
        LocationReview locationReview = locationReviewRepository.findByLocationId(locationId)
            .orElse(new LocationReview());
        
        if (locationReview.getLocationReviewId() != null) {
            System.out.println("🔥 DEBUG: Found existing LocationReview, updating it");
        } else {
            System.out.println("🔥 DEBUG: No existing LocationReview found, creating new one");
        }
        
        locationReview.setLocation(location);
        locationReview.setAverageRating(BigDecimal.valueOf(averageRating));
        locationReview.setTotalReviews(totalReviews);
        locationReview.setRating1Count(rating1Count);
        locationReview.setRating2Count(rating2Count);
        locationReview.setRating3Count(rating3Count);
        locationReview.setRating4Count(rating4Count);
        locationReview.setRating5Count(rating5Count);
        
        LocationReview savedReview = locationReviewRepository.save(locationReview);
        System.out.println("🔥 DEBUG: Saved LocationReview with ID: " + savedReview.getLocationReviewId());
    }
    
    /**
     * Được gọi khi có review mới cho một sân
     * @param fieldId ID của sân vừa được review
     */
    @Transactional
    public void onNewFieldReview(Long fieldId) {
        System.out.println("🔥 DEBUG: onNewFieldReview called for fieldId: " + fieldId);
        Optional<Field> fieldOpt = fieldRepository.findById(fieldId);
        if (fieldOpt.isPresent()) {
            Field field = fieldOpt.get();
            Long locationId = field.getLocation().getLocationId();
            System.out.println("🔥 DEBUG: Found field, locationId: " + locationId);
            updateLocationReviewFromFieldReviews(locationId);
        } else {
            System.out.println("❌ DEBUG: Field not found for fieldId: " + fieldId);
        }
    }
    
    /**
     * Lấy location review theo location ID
     * @param locationId ID của location
     * @return Optional LocationReview
     */
    public Optional<LocationReview> getLocationReview(Long locationId) {
        return locationReviewRepository.findByLocationId(locationId);
    }
    
    /**
     * Lấy location reviews dưới dạng DTO
     * @param locationId ID của location
     * @return Danh sách LocationReviewDTO
     */
    public List<LocationReviewDTO> getLocationReviewDTOs(Long locationId) {
        List<Review> reviews = reviewRepository.findByFieldLocationId(locationId);
        return locationReviewMapper.toDtoList(reviews);
    }
    
    /**
     * Lấy thống kê rating của location
     * @param locationId ID của location
     * @return FieldRatingResponse chứa thống kê
     */
    public FieldRatingResponse getLocationRatingStats(Long locationId) {
        Optional<LocationReview> locationReview = locationReviewRepository.findByLocationId(locationId);
        
        FieldRatingResponse response = new FieldRatingResponse();
        
        if (locationReview.isPresent()) {
            LocationReview review = locationReview.get();
            response.setAverageRating(review.getAverageRating().doubleValue());
            response.setTotalReviews(review.getTotalReviews());
            response.setRating1Count(review.getRating1Count());
            response.setRating2Count(review.getRating2Count());
            response.setRating3Count(review.getRating3Count());
            response.setRating4Count(review.getRating4Count());
            response.setRating5Count(review.getRating5Count());
        } else {
            response.setAverageRating(0.0);
            response.setTotalReviews(0L);
            response.setRating1Count(0L);
            response.setRating2Count(0L);
            response.setRating3Count(0L);
            response.setRating4Count(0L);
            response.setRating5Count(0L);
        }
        
        return response;
    }
    
    /**
     * Tính toán lại tất cả location reviews (có thể dùng cho migration hoặc fix data)
     */
    @Transactional
    public void recalculateAllLocationReviews() {
        List<Location> allLocations = locationRepository.findAll();
        
        for (Location location : allLocations) {
            updateLocationReviewFromFieldReviews(location.getLocationId());
        }
    }
    

}