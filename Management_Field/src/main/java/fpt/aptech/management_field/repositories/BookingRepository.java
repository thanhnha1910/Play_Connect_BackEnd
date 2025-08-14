package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.bookingUsers WHERE b.field.fieldId = :fieldId AND b.fromTime >= :startDate AND b.toTime <= :endDate AND b.status != 'cancelled'")
    List<Booking> findForFieldByDate(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate, @Param("fieldId") Long fieldId);
    List<Booking> findByUserId(Long userId);
    
    List<Booking> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    Optional<Booking> findByPaymentToken(String token);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.field = :field " +
            "AND b.status != 'cancelled' " +
            "AND b.fromTime < :toTime AND b.toTime > :fromTime")
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
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.status = :status")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    // Owner bookings management queries
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.field f " +
           "JOIN FETCH f.location l " +
           "JOIN FETCH l.owner " +
           "WHERE l.owner.user.id = :ownerId " +
           "AND (:startDate IS NULL OR b.fromTime >= :startDate) " +
           "AND (:endDate IS NULL OR b.toTime <= :endDate) " +
           "AND (:status IS NULL OR b.status = :status) " +
           "AND (:facilityId IS NULL OR l.locationId = :facilityId) " +
           "AND (:searchQuery IS NULL OR LOWER(b.user.username) LIKE LOWER(CONCAT('%', :searchQuery, '%')) " +
           "     OR LOWER(f.name) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) " +
           "ORDER BY b.fromTime DESC")
    List<Booking> findOwnerBookings(@Param("ownerId") Long ownerId,
                                   @Param("startDate") Instant startDate,
                                   @Param("endDate") Instant endDate,
                                   @Param("status") String status,
                                   @Param("facilityId") Long facilityId,
                                   @Param("searchQuery") String searchQuery);
    
    // Owner bookings management queries with pagination
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.field f " +
           "JOIN FETCH f.location l " +
           "JOIN FETCH l.owner " +
           "WHERE l.owner.user.id = :ownerId " +
           "AND (:startDate IS NULL OR b.fromTime >= :startDate) " +
           "AND (:endDate IS NULL OR b.toTime <= :endDate) " +
           "AND (:status IS NULL OR b.status = :status) " +
           "AND (:facilityId IS NULL OR l.locationId = :facilityId) " +
           "AND (:searchQuery IS NULL OR LOWER(b.user.username) LIKE LOWER(CONCAT('%', :searchQuery, '%')) " +
           "     OR LOWER(f.name) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) " +
           "ORDER BY b.fromTime DESC")
    org.springframework.data.domain.Page<Booking> findOwnerBookingsWithPagination(
                                   @Param("ownerId") Long ownerId,
                                   @Param("startDate") Instant startDate,
                                   @Param("endDate") Instant endDate,
                                   @Param("status") String status,
                                   @Param("facilityId") Long facilityId,
                                   @Param("searchQuery") String searchQuery,
                                   org.springframework.data.domain.Pageable pageable);
    
    // Count owner bookings for pagination
    @Query("SELECT COUNT(b) FROM Booking b " +
           "JOIN b.field f " +
           "JOIN f.location l " +
           "JOIN l.owner " +
           "WHERE l.owner.user.id = :ownerId " +
           "AND (:startDate IS NULL OR b.fromTime >= :startDate) " +
           "AND (:endDate IS NULL OR b.toTime <= :endDate) " +
           "AND (:status IS NULL OR b.status = :status) " +
           "AND (:facilityId IS NULL OR l.locationId = :facilityId) " +
           "AND (:searchQuery IS NULL OR LOWER(b.user.username) LIKE LOWER(CONCAT('%', :searchQuery, '%')) " +
           "     OR LOWER(f.name) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
    long countOwnerBookings(@Param("ownerId") Long ownerId,
                           @Param("startDate") Instant startDate,
                           @Param("endDate") Instant endDate,
                           @Param("status") String status,
                           @Param("facilityId") Long facilityId,
                           @Param("searchQuery") String searchQuery);
    
    // Count total bookings for owner
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.field.location.owner.user.id = :ownerId")
    long countByOwner(@Param("ownerId") Long ownerId);
    
    // Count upcoming bookings for owner
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.field.location.owner.user.id = :ownerId " +
           "AND b.fromTime > :now")
    long countUpcomingByOwner(@Param("ownerId") Long ownerId, @Param("now") Instant now);
    
    // Count pending bookings for owner
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.field.location.owner.user.id = :ownerId " +
           "AND b.status = 'pending'")
    long countPendingByOwner(@Param("ownerId") Long ownerId);
    
    // Calculate this month revenue for owner by facility
    // Sửa query calculateMonthlyRevenueByFacility
    @Query("SELECT COALESCE(SUM(p.ownerAmount), 0) " +
           "FROM Booking b " +
           "JOIN Payment p ON p.payableId = b.bookingId AND p.payableType = 'BOOKING' " +
           "WHERE b.field.location.owner.user.id = :ownerId " +
           "AND b.status = 'confirmed' " +
           "AND YEAR(b.fromTime) = :year " +
           "AND MONTH(b.fromTime) = :month " +
           "AND (:facilityId IS NULL OR b.field.location.locationId = :facilityId) " +
           "AND p.paymentId = (SELECT MIN(p2.paymentId) FROM Payment p2 WHERE p2.payableId = b.bookingId AND p2.payableType = 'BOOKING')")
    double calculateMonthlyRevenueByFacility(@Param("ownerId") Long ownerId,
                                            @Param("year") int year,
                                            @Param("month") int month,
                                            @Param("facilityId") Long facilityId);
    
    // Count methods with facility filter
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.field.location.owner.user.id = :ownerId " +
           "AND (:facilityId IS NULL OR b.field.location.locationId = :facilityId)")
    long countByOwnerAndFacility(@Param("ownerId") Long ownerId, @Param("facilityId") Long facilityId);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.field.location.owner.user.id = :ownerId " +
           "AND b.fromTime > :now " +
           "AND (:facilityId IS NULL OR b.field.location.locationId = :facilityId)")
    long countUpcomingByOwnerAndFacility(@Param("ownerId") Long ownerId, 
                                       @Param("now") Instant now,
                                       @Param("facilityId") Long facilityId);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.field.location.owner.user.id = :ownerId " +
           "AND b.status = 'pending' " +
           "AND (:facilityId IS NULL OR b.field.location.locationId = :facilityId)")
    long countPendingByOwnerAndFacility(@Param("ownerId") Long ownerId, @Param("facilityId") Long facilityId);
    
    // Dashboard specific queries
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.field f " +
           "JOIN FETCH f.location l " +
           "WHERE l.owner.user.id = :ownerId " +
           "AND CAST(b.fromTime AS DATE) = CAST(CURRENT_TIMESTAMP AS DATE) " +
           "ORDER BY b.fromTime ASC")
    List<Booking> findTodayBookingsByOwner(@Param("ownerId") Long ownerId);
    
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.field f " +
           "JOIN FETCH f.location l " +
           "WHERE l.owner.user.id = :ownerId " +
           "AND b.fromTime >= :now " +
           "AND b.fromTime <= :endOfDay " +
           "AND b.status = 'confirmed' " +
           "ORDER BY b.fromTime ASC")
    List<Booking> findUpcomingBookingsByOwner(@Param("ownerId") Long ownerId, 
                                             @Param("now") Instant now, 
                                             @Param("endOfDay") Instant endOfDay);
    
    @Query("SELECT COALESCE(SUM(p.ownerAmount), 0) " +
           "FROM Booking b " +
           "JOIN Payment p ON p.payableId = b.bookingId AND p.payableType = 'BOOKING' " +
           "WHERE b.field.location.owner.user.id = :ownerId " +
           "AND b.status = 'confirmed' " +
           "AND CAST(b.fromTime AS DATE) = CAST(CURRENT_TIMESTAMP AS DATE) " +
           "AND p.paymentId = (SELECT MIN(p2.paymentId) FROM Payment p2 WHERE p2.payableId = b.bookingId AND p2.payableType = 'BOOKING')")
    double calculateTodayRevenueByOwner(@Param("ownerId") Long ownerId);
    
    @Query("SELECT COALESCE(SUM(p.ownerAmount), 0) " +
           "FROM Booking b " +
           "JOIN Payment p ON p.payableId = b.bookingId AND p.payableType = 'BOOKING' " +
           "WHERE b.field.location.owner.user.id = :ownerId " +
           "AND b.status = 'confirmed' " +
           "AND b.fromTime >= :startDate " +
           "AND b.fromTime <= :endDate " +
           "AND p.paymentId = (SELECT MIN(p2.paymentId) FROM Payment p2 WHERE p2.payableId = b.bookingId AND p2.payableType = 'BOOKING')")
    double calculateRevenueByOwnerAndDateRange(@Param("ownerId") Long ownerId,
                                              @Param("startDate") Instant startDate,
                                              @Param("endDate") Instant endDate);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.field.location.owner.user.id = :ownerId " +
           "AND b.status = 'confirmed' " +
           "AND b.fromTime >= :startDate " +
           "AND b.fromTime <= :endDate")
    long countConfirmedBookingsByOwnerAndDateRange(@Param("ownerId") Long ownerId,
                                                  @Param("startDate") Instant startDate,
                                                  @Param("endDate") Instant endDate);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.field.location.owner.user.id = :ownerId " +
           "AND b.status = 'confirmed' " +
           "AND CAST(b.fromTime AS DATE) = CAST(CURRENT_TIMESTAMP AS DATE)")
    long countTodayConfirmedBookingsByOwner(@Param("ownerId") Long ownerId);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.field.location.owner.user.id = :ownerId " +
           "AND b.status = 'pending' " +
           "AND CAST(b.fromTime AS DATE) = CAST(CURRENT_TIMESTAMP AS DATE)")
    long countTodayPendingBookingsByOwner(@Param("ownerId") Long ownerId);
    
    // Revenue trend query
    @Query("SELECT CAST(b.fromTime AS DATE) as date, " +
           "COALESCE(SUM(p.ownerAmount), 0) as revenue " +
           "FROM Booking b " +
           "JOIN Payment p ON p.payableId = b.bookingId AND p.payableType = 'BOOKING' " +
           "WHERE b.field.location.owner.user.id = :ownerId " +
           "AND b.status = 'confirmed' " +
           "AND b.fromTime >= :startDate " +
           "AND b.fromTime <= :endDate " +
           "AND p.paymentId = (SELECT MIN(p2.paymentId) FROM Payment p2 WHERE p2.payableId = b.bookingId AND p2.payableType = 'BOOKING') " +
           "GROUP BY CAST(b.fromTime AS DATE) " +
           "ORDER BY CAST(b.fromTime AS DATE)")
    List<Object[]> getRevenueTrendByOwner(@Param("ownerId") Long ownerId,
                                         @Param("startDate") Instant startDate,
                                         @Param("endDate") Instant endDate);
    
    // Performance by facility query
    @Query("SELECT l.name as facilityName, " +
           "COALESCE(SUM(p.ownerAmount), 0) as revenue, " +
           "COUNT(b) as bookingCount " +
           "FROM Booking b " +
           "JOIN b.field f " +
           "JOIN f.location l " +
           "JOIN Payment p ON p.payableId = b.bookingId AND p.payableType = 'BOOKING' " +
           "WHERE l.owner.user.id = :ownerId " +
           "AND b.status = 'confirmed' " +
           "AND b.fromTime >= :startDate " +
           "AND b.fromTime <= :endDate " +
           "AND p.paymentId = (SELECT MIN(p2.paymentId) FROM Payment p2 WHERE p2.payableId = b.bookingId AND p2.payableType = 'BOOKING') " +
           "GROUP BY l.locationId, l.name " +
           "ORDER BY revenue DESC")
    List<Object[]> getPerformanceByFacility(@Param("ownerId") Long ownerId,
                                           @Param("startDate") Instant startDate,
                                           @Param("endDate") Instant endDate);
    
    // Performance by field query
    @Query("SELECT f.name as fieldName, " +
           "COALESCE(SUM(p.ownerAmount), 0) as revenue, " +
           "COUNT(b) as bookingCount " +
           "FROM Booking b " +
           "JOIN b.field f " +
           "JOIN Payment p ON p.payableId = b.bookingId AND p.payableType = 'BOOKING' " +
           "WHERE f.location.owner.user.id = :ownerId " +
           "AND b.status = 'confirmed' " +
           "AND b.fromTime >= :startDate " +
           "AND b.fromTime <= :endDate " +
           "AND p.paymentId = (SELECT MIN(p2.paymentId) FROM Payment p2 WHERE p2.payableId = b.bookingId AND p2.payableType = 'BOOKING') " +
           "GROUP BY f.fieldId, f.name " +
           "ORDER BY revenue DESC")
    List<Object[]> getPerformanceByField(@Param("ownerId") Long ownerId,
                                        @Param("startDate") Instant startDate,
                                        @Param("endDate") Instant endDate);
    
    // Facility-specific analytics methods
    @Query("SELECT COALESCE(SUM(p.ownerAmount), 0) " +
           "FROM Booking b " +
           "JOIN Payment p ON p.payableId = b.bookingId AND p.payableType = 'BOOKING' " +
           "WHERE b.field.location.locationId = :facilityId " +
           "AND b.status = 'confirmed' " +
           "AND b.fromTime >= :startDate " +
           "AND b.fromTime <= :endDate " +
           "AND p.paymentId = (SELECT MIN(p2.paymentId) FROM Payment p2 WHERE p2.payableId = b.bookingId AND p2.payableType = 'BOOKING')")
    double calculateRevenueByFacilityAndDateRange(@Param("facilityId") Long facilityId,
                                                 @Param("startDate") Instant startDate,
                                                 @Param("endDate") Instant endDate);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.field.location.locationId = :facilityId " +
           "AND b.status = 'confirmed' " +
           "AND b.fromTime >= :startDate " +
           "AND b.fromTime <= :endDate")
    long countConfirmedBookingsByFacilityAndDateRange(@Param("facilityId") Long facilityId,
                                                     @Param("startDate") Instant startDate,
                                                     @Param("endDate") Instant endDate);
    
    @Query("SELECT CAST(b.fromTime AS DATE) as date, " +
           "COALESCE(SUM(p.ownerAmount), 0) as revenue " +
           "FROM Booking b " +
           "JOIN Payment p ON p.payableId = b.bookingId AND p.payableType = 'BOOKING' " +
           "WHERE b.field.location.locationId = :facilityId " +
           "AND b.status = 'confirmed' " +
           "AND b.fromTime >= :startDate " +
           "AND b.fromTime <= :endDate " +
           "AND p.paymentId = (SELECT MIN(p2.paymentId) FROM Payment p2 WHERE p2.payableId = b.bookingId AND p2.payableType = 'BOOKING') " +
           "GROUP BY CAST(b.fromTime AS DATE) " +
           "ORDER BY CAST(b.fromTime AS DATE)")
    List<Object[]> getRevenueTrendByFacility(@Param("facilityId") Long facilityId,
                                            @Param("startDate") Instant startDate,
                                            @Param("endDate") Instant endDate);
    
    @Query("SELECT f.name as fieldName, " +
           "COALESCE(SUM(p.ownerAmount), 0) as revenue, " +
           "COUNT(b) as bookingCount " +
           "FROM Booking b " +
           "JOIN b.field f " +
           "JOIN Payment p ON p.payableId = b.bookingId AND p.payableType = 'BOOKING' " +
           "WHERE f.location.locationId = :facilityId " +
           "AND b.status = 'confirmed' " +
           "AND b.fromTime >= :startDate " +
           "AND b.fromTime <= :endDate " +
           "AND p.paymentId = (SELECT MIN(p2.paymentId) FROM Payment p2 WHERE p2.payableId = b.bookingId AND p2.payableType = 'BOOKING') " +
           "GROUP BY f.fieldId, f.name " +
           "ORDER BY revenue DESC")
    List<Object[]> getPerformanceByFieldInFacility(@Param("facilityId") Long facilityId,
                                                  @Param("startDate") Instant startDate,
                                                  @Param("endDate") Instant endDate);
    
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.field f " +
           "WHERE f.location.locationId = :facilityId " +
           "ORDER BY b.fromTime DESC")
    List<Booking> findRecentBookingsByFacility(@Param("facilityId") Long facilityId,
                                              org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.field f " +
           "JOIN FETCH f.location l " +
           "WHERE l.owner.user.id = :ownerId " +
           "ORDER BY b.fromTime DESC")
    List<Booking> findRecentBookingsByOwner(@Param("ownerId") Long ownerId,
                                           org.springframework.data.domain.Pageable pageable);

             //admin 
                 @Query("SELECT b.status, COUNT(b) FROM Booking b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate GROUP BY b.status")
    List<Object[]> getBookingStatusStats(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status AND b.createdAt >= :startDate AND b.createdAt <= :endDate")
    Long getBookingCountByStatus(@Param("status") String status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b.field.fieldId, b.field.name, COUNT(b) as bookingCount " +
            "FROM Booking b WHERE b.status = 'confirmed' AND b.createdAt >= :startDate AND b.createdAt <= :endDate " +
            "GROUP BY b.field.fieldId, b.field.name ORDER BY COUNT(b) DESC")
    List<Object[]> getTopBookedFields(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT EXTRACT(HOUR FROM b.fromTime) as hour, COUNT(b) " +
            "FROM Booking b WHERE b.status = 'confirmed' AND b.createdAt >= :startDate AND b.createdAt <= :endDate " +
            "GROUP BY EXTRACT(HOUR FROM b.fromTime) ORDER BY EXTRACT(HOUR FROM b.fromTime)")
    List<Object[]> getBookingsByHour(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DATE(b.createdAt) as date, COUNT(b) as count " +
            "FROM Booking b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate " +
            "GROUP BY DATE(b.createdAt) ORDER BY DATE(b.createdAt)")
    List<Object[]> getDailyBookingCounts(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find completed bookings for review notifications
    @Query("SELECT b FROM Booking b " +
           "WHERE b.status = 'confirmed' " +
           "AND b.toTime <= :cutoffTime " +
           "ORDER BY b.toTime DESC")
    List<Booking> findCompletedBookingsForReview(@Param("cutoffTime") Instant cutoffTime);
}
