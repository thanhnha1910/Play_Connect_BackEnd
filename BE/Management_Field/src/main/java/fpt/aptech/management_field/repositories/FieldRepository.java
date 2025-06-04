package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {
    
    // Find fields within radius using Haversine formula
    @Query(value = "SELECT f.* FROM fields f " +
           "JOIN locations l ON f.location_id = l.location_id " +
           "WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(l.latitude)) * " +
           "cos(radians(l.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(l.latitude)))) <= :radiusKm", 
           nativeQuery = true)
    List<Field> findFieldsWithinRadius(@Param("latitude") BigDecimal latitude, 
                                      @Param("longitude") BigDecimal longitude, 
                                      @Param("radiusKm") Double radiusKm);
    
    // Find fields within bounds
    @Query("SELECT f FROM Field f JOIN f.location l " +
           "WHERE l.latitude BETWEEN :southWestLat AND :northEastLat " +
           "AND l.longitude BETWEEN :southWestLng AND :northEastLng")
    List<Field> findFieldsWithinBounds(@Param("southWestLat") BigDecimal southWestLat,
                                      @Param("southWestLng") BigDecimal southWestLng,
                                      @Param("northEastLat") BigDecimal northEastLat,
                                      @Param("northEastLng") BigDecimal northEastLng);
    
    // Find fields with filters
    @Query("SELECT f FROM Field f WHERE " +
           "(:typeId IS NULL OR f.type.typeId = :typeId) AND " +
           "(:categoryId IS NULL OR f.category.categoryId = :categoryId) AND " +
           "(:minHourlyRate IS NULL OR f.hourlyRate >= :minHourlyRate) AND " +
           "(:maxHourlyRate IS NULL OR f.hourlyRate <= :maxHourlyRate)")
    List<Field> findFieldsWithFilters(@Param("typeId") Long typeId,
                                     @Param("categoryId") Long categoryId,
                                     @Param("minHourlyRate") Integer minHourlyRate,
                                     @Param("maxHourlyRate") Integer maxHourlyRate);
}