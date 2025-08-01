package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Thêm import này
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long>, JpaSpecificationExecutor<Field> { // Thêm JpaSpecificationExecutor<Field>

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

    // Find fields with filters (Query này có thể không cần thiết nữa nếu bạn dùng Specification hoàn toàn cho getFieldsByLocation)
    // Nếu vẫn muốn giữ, hãy đảm bảo nó không xung đột với logic Specification
    @Query("SELECT f FROM Field f WHERE " +
            "(:typeId IS NULL OR f.type.typeId = :typeId) AND " +
            "(:categoryId IS NULL OR f.category.categoryId = :categoryId) AND " +
            "(:minHourlyRate IS NULL OR f.hourlyRate >= :minHourlyRate) AND " +
            "(:maxHourlyRate IS NULL OR f.hourlyRate <= :maxHourlyRate)")
    List<Field> findFieldsWithFilters(@Param("typeId") Long typeId,
                                      @Param("categoryId") Long categoryId,
                                      @Param("minHourlyRate") Integer minHourlyRate,
                                      @Param("maxHourlyRate") Integer maxHourlyRate);

    @Query(value = "SELECT f FROM Field f WHERE f.location.locationId = :locationId")
    List<Field> getFieldsByLocationId(@Param("locationId") Long locationId);
    
    // Find fields by owner's user ID
    @Query("SELECT f FROM Field f WHERE f.location.owner.user.id = :userId")
    List<Field> findByLocation_Owner_User_Id(@Param("userId") Long userId);
    
    // Check if field belongs to specific user
    @Query("SELECT COUNT(f) > 0 FROM Field f WHERE f.fieldId = :fieldId AND f.location.owner.user.id = :userId")
    boolean existsByFieldIdAndOwnerUserId(@Param("fieldId") Long fieldId, @Param("userId") Long userId);
    
    // Find fields by location ID
    List<Field> findByLocation_LocationId(Long locationId);


    @Query("SELECT f FROM Field f WHERE f.isActive = true AND f.fieldId NOT IN " +
            "(SELECT b.field.fieldId FROM Booking b WHERE (b.fromTime <= :toTime AND b.toTime >= :fromTime)) " +
            "AND (:locationId IS NULL OR f.location.locationId = :locationId)")
    List<Field> findAvailableFields(@Param("fromTime") LocalDateTime fromTime,
                                    @Param("toTime") LocalDateTime toTime,
                                    @Param("locationId") Long locationId);

}