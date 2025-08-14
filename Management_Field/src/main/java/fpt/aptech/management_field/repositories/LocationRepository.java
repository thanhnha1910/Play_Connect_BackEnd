package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    // Find locations within radius using Haversine formula
    @Query(value = "SELECT l.* FROM locations l " +
           "WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(l.latitude)) * " +
           "cos(radians(l.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(l.latitude)))) <= :radiusKm",
           nativeQuery = true)
    List<Location> findLocationsWithinRadius(@Param("latitude") BigDecimal latitude,
                                           @Param("longitude") BigDecimal longitude,
                                           @Param("radiusKm") Double radiusKm);
    
    // Find locations within radius with field filters
    @Query(value = "SELECT DISTINCT l.* FROM locations l " +
           "JOIN fields f ON l.location_id = f.location_id " +
           "WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(l.latitude)) * " +
           "cos(radians(l.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(l.latitude)))) <= :radiusKm " +
           "AND (:typeId IS NULL OR f.type_id = :typeId) " +
           "AND (:categoryId IS NULL OR f.category_id = :categoryId) " +
           "AND (:minHourlyRate IS NULL OR f.hourly_rate >= :minHourlyRate) " +
           "AND (:maxHourlyRate IS NULL OR f.hourly_rate <= :maxHourlyRate)",
           nativeQuery = true)
    List<Location> findLocationsWithinRadiusWithFilters(@Param("latitude") BigDecimal latitude,
                                                       @Param("longitude") BigDecimal longitude,
                                                       @Param("radiusKm") Double radiusKm,
                                                       @Param("typeId") Long typeId,
                                                       @Param("categoryId") Long categoryId,
                                                       @Param("minHourlyRate") Integer minHourlyRate,
                                                       @Param("maxHourlyRate") Integer maxHourlyRate);
    
    // Count fields for a location
    @Query("SELECT COUNT(f) FROM Field f WHERE f.location.locationId = :locationId")
    Integer countFieldsByLocationId(@Param("locationId") Long locationId);
    
    // Find locations by owner's user ID
    List<Location> findByOwner_User_Id(Long userId);
    
    // Find location by ID with owner eagerly loaded to avoid LazyInitializationException
    @Query("SELECT l FROM Location l JOIN FETCH l.owner o JOIN FETCH o.user WHERE l.locationId = :locationId")
    java.util.Optional<Location> findByIdWithOwner(@Param("locationId") Long locationId);
    
    // Get average rating for a location (returns null if table doesn't exist)
    @Query(value = "SELECT CASE WHEN EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'location_reviews') " +
           "THEN (SELECT AVG(CAST(lr.average_rating AS DECIMAL(3,2))) FROM location_reviews lr WHERE lr.location_id = :locationId) " +
           "ELSE NULL END",
           nativeQuery = true)
    BigDecimal getAverageRatingByLocationId(@Param("locationId") Long locationId);
    
    // Get minimum hourly rate for a location
    @Query("SELECT MIN(f.hourlyRate) FROM Field f WHERE f.location.locationId = :locationId")
    Integer getMinimumHourlyRateByLocationId(@Param("locationId") Long locationId);

    @Query("SELECT l FROM Location l WHERE l.slug = :slug")
    Location getLocationBySlug(@Param("slug") String slug);
    
    @Query("SELECT l FROM Location l WHERE LOWER(l.slug) = LOWER(:slug)")
    Location getLocationBySlugIgnoreCase(@Param("slug") String slug);
    
    @Query(value = "SELECT * FROM locations WHERE slug = :slug", nativeQuery = true)
    Location getLocationBySlugNative(@Param("slug") String slug);
    
    @Query("SELECT l FROM Location l")
    List<Location> findAllLocations();
    
    // Find all locations sorted by average rating (highest first)
    @Query("SELECT l FROM Location l " +
           "LEFT JOIN LocationReview lr ON l.locationId = lr.location.locationId " +
           "GROUP BY l.locationId, l.name, l.address, l.description, l.city, l.country, l.latitude, l.longitude, l.slug, l.owner, l.thumbnailUrl, l.imageGallery " +
           "ORDER BY AVG(lr.averageRating) DESC NULLS LAST")
    List<Location> findAllSortedByRating();
    
    // Find all locations sorted by booking count in last 30 days (most popular first)
    @Query("SELECT l FROM Location l " +
           "LEFT JOIN Field f ON l.locationId = f.location.locationId " +
           "LEFT JOIN Booking b ON f.fieldId = b.field.fieldId " +
           "AND b.fromTime > :thirtyDaysAgo " +
           "GROUP BY l.locationId, l.name, l.address, l.description, l.city, l.country, l.latitude, l.longitude, l.slug, l.owner, l.thumbnailUrl, l.imageGallery " +
           "ORDER BY COUNT(b.bookingId) DESC")
    List<Location> findAllSortedByPopularity(@Param("thirtyDaysAgo") java.time.Instant thirtyDaysAgo);
    
    // Get booking count for a location in last 30 days
    @Query("SELECT COUNT(b) FROM Booking b " +
           "JOIN b.field f " +
           "WHERE f.location.locationId = :locationId " +
           "AND b.fromTime > :thirtyDaysAgo")
    Long getBookingCountByLocationId(@Param("locationId") Long locationId, 
                                   @Param("thirtyDaysAgo") java.time.Instant thirtyDaysAgo);
   
}