package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.FieldType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldTypeRepository extends JpaRepository<FieldType, Long> {
    
    // Find all field types for a specific location
    List<FieldType> findByLocation_LocationId(Long locationId);
    
    // Find field types by location and owner
    @Query("SELECT ft FROM FieldType ft WHERE ft.location.locationId = :locationId AND ft.location.owner.user.id = :ownerId")
    List<FieldType> findByLocationIdAndOwnerId(@Param("locationId") Long locationId, @Param("ownerId") Long ownerId);
    
    // Check if field type exists and belongs to owner
    @Query("SELECT COUNT(ft) > 0 FROM FieldType ft WHERE ft.typeId = :fieldTypeId AND ft.location.owner.user.id = :ownerId")
    boolean existsByFieldTypeIdAndOwnerId(@Param("fieldTypeId") Long fieldTypeId, @Param("ownerId") Long ownerId);
}