package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Find all reviews for a specific field
    @Query("SELECT r FROM Review r WHERE r.field.fieldId = :fieldId ORDER BY r.createdAt DESC")
    List<Review> findByFieldIdOrderByCreatedAtDesc(@Param("fieldId") Long fieldId);
    
    // Find reviews by user
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Review> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    // Check if user already reviewed a specific booking
    @Query("SELECT r FROM Review r WHERE r.booking.bookingId = :bookingId AND r.user.id = :userId")
    Optional<Review> findByBookingIdAndUserId(@Param("bookingId") Long bookingId, @Param("userId") Long userId);
    
    // Check if booking already has a review
    boolean existsByBooking_BookingId(Long bookingId);
    
    // Get average rating for a field
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.field.fieldId = :fieldId")
    Double getAverageRatingByFieldId(@Param("fieldId") Long fieldId);
    
    // Count reviews for a field
    @Query("SELECT COUNT(r) FROM Review r WHERE r.field.fieldId = :fieldId")
    Long countReviewsByFieldId(@Param("fieldId") Long fieldId);
    
    // Get reviews for fields owned by a specific owner
    @Query("SELECT r FROM Review r WHERE r.field.location.owner.user.id = :ownerId ORDER BY r.createdAt DESC")
    List<Review> findReviewsForOwnerFields(@Param("ownerId") Long ownerId);
    
    // Get recent reviews for a field (limit)
    @Query("SELECT r FROM Review r WHERE r.field.fieldId = :fieldId ORDER BY r.createdAt DESC")
    List<Review> findRecentReviewsByFieldId(@Param("fieldId") Long fieldId, org.springframework.data.domain.Pageable pageable);
    
    // Get all reviews for multiple fields
    @Query("SELECT r FROM Review r WHERE r.field.fieldId IN :fieldIds")
    List<Review> findByField_FieldIdIn(@Param("fieldIds") List<Long> fieldIds);
    
    // Get all reviews for fields in a specific location
    @Query("SELECT r FROM Review r WHERE r.field.location.locationId = :locationId ORDER BY r.createdAt DESC")
    List<Review> findByFieldLocationId(@Param("locationId") Long locationId);
}