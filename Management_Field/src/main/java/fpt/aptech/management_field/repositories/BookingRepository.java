package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.field.fieldId = :fieldId AND b.fromTime >= :startDate AND b.toTime <= :endDate")
    List<Booking> findForFieldByDate(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate, @Param("fieldId") Long fieldId);
    List<Booking> findByUserId(Long userId);

    Optional<Booking> findByPaymentToken(String token);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.field = :field " +
            "AND b.fromTime <= :toTime AND b.toTime >= :fromTime")
    boolean existsByFieldAndFromTimeLessThanEqualAndToTimeGreaterThanEqual(
            @Param("field") Field field,
            @Param("toTime") Instant toTime,
            @Param("fromTime") Instant fromTime);

    @Query("SELECT b FROM Booking b WHERE b.fromTime >= :now AND b.fromTime <= :twoHoursLater " +
            "AND b.status = 'confirmed' AND (b.reminderSent = false OR b.reminderSent IS NULL)")
    List<Booking> findUpcomingBookingsForReminder(
            @Param("now") Instant now,
            @Param("twoHoursLater") Instant twoHoursLater);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.fromTime DESC")
    List<Booking> findByUserIdOrderByFromTimeDesc(@Param("userId") Long userId);
}
