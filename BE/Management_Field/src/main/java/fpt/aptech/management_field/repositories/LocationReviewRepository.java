package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.LocationReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationReviewRepository extends JpaRepository<LocationReview, Long> {
    @Query(value = "SELECT lr FROM LocationReview lr WHERE lr.location.locationId = :locationId")
    List<LocationReview> findByLocationId(@Param("locationId") Long locationId);
}
