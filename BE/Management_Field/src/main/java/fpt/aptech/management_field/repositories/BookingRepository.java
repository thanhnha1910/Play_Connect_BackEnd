package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query(value = "SELECT b FROM Booking b WHERE b.fromTime >= :startDate AND b.toTime <= :endDate AND b.field.fieldId = :fieldId")
    List<Booking> findForFieldByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("fieldId") Long fieldId);
}
