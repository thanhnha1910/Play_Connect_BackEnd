package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.LocationReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationReviewRepository extends JpaRepository<LocationReview, Long> {
    
    // Find all reviews for a specific location (for backward compatibility)
    @Query("SELECT lr FROM LocationReview lr WHERE lr.location.locationId = :locationId ORDER BY lr.locationReviewId DESC")
    List<LocationReview> findByLocationIdOrderByIdDesc(@Param("locationId") Long locationId);
    
    // Find single location review by location (since there's only one per location now)
    @Query("SELECT lr FROM LocationReview lr WHERE lr.location.locationId = :locationId")
    Optional<LocationReview> findByLocationId(@Param("locationId") Long locationId);
    
    // Delete location review by location ID
    @Modifying
    @Query("DELETE FROM LocationReview lr WHERE lr.location.locationId = :locationId")
    void deleteByLocationId(@Param("locationId") Long locationId);
    
    // Get reviews for locations owned by a specific owner
    @Query("SELECT lr FROM LocationReview lr WHERE lr.location.owner.user.id = :ownerId ORDER BY lr.locationReviewId DESC")
    List<LocationReview> findReviewsForOwnerLocations(@Param("ownerId") Long ownerId);
    
    // Get recent reviews for a location (limit)
    @Query("SELECT lr FROM LocationReview lr WHERE lr.location.locationId = :locationId ORDER BY lr.locationReviewId DESC")
    List<LocationReview> findRecentReviewsByLocationId(@Param("locationId") Long locationId, org.springframework.data.domain.Pageable pageable);
}
