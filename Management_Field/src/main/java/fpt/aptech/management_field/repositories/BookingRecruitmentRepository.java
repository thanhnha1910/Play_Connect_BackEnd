package fpt.aptech.management_field.repositories;


import fpt.aptech.management_field.models.BookingRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BookingRecruitmentRepository extends JpaRepository<BookingRequirement, Long> {
    // Get all recruitments by user ID
    List<BookingRequirement> findByUserId(Long userId);
}
