package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.AdminRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminRevenueRepository extends JpaRepository<AdminRevenue, Long>, JpaSpecificationExecutor<AdminRevenue> {

    // Summary Queries
    @Query("SELECT SUM(ar.commissionAmount) FROM AdminRevenue ar WHERE ar.createdAt >= :startDate AND ar.createdAt <= :endDate")
    Long getTotalCommissionByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(ar.bookingAmount) FROM AdminRevenue ar WHERE ar.createdAt >= :startDate AND ar.createdAt <= :endDate")
    Long getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(ar) FROM AdminRevenue ar WHERE ar.createdAt >= :startDate AND ar.createdAt <= :endDate")
    Long getBookingCountByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(ar.bookingAmount) FROM AdminRevenue ar WHERE ar.createdAt >= :startDate AND ar.createdAt <= :endDate")
    Double getAverageBookingValueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Daily Revenue Data
    @Query(value = "SELECT CAST(ar.created_at AS DATE) as date, " +
            "SUM(ar.commission_amount) as commission, " +
            "SUM(ar.booking_amount) as revenue, " +
            "COUNT_BIG(ar.id) as bookings " +
            "FROM admin_revenues ar " +
            "WHERE ar.created_at >= :startDate AND ar.created_at <= :endDate " +
            "GROUP BY CAST(ar.created_at AS DATE) " +
            "ORDER BY CAST(ar.created_at AS DATE)",
            nativeQuery = true)
    List<Object[]> getDailyRevenueData(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // Monthly Revenue Data
    @Query("SELECT YEAR(ar.createdAt) as year, MONTH(ar.createdAt) as month, SUM(ar.commissionAmount) as commission, SUM(ar.bookingAmount) as revenue, COUNT(ar) as bookings " +
            "FROM AdminRevenue ar WHERE ar.createdAt >= :startDate AND ar.createdAt <= :endDate " +
            "GROUP BY YEAR(ar.createdAt), MONTH(ar.createdAt) ORDER BY YEAR(ar.createdAt), MONTH(ar.createdAt)")
    List<Object[]> getMonthlyRevenueData(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Top Performing Fields
    @Query("SELECT ar.fieldName, COUNT(ar) as bookings, SUM(ar.commissionAmount) as commission, SUM(ar.bookingAmount) as revenue " +
            "FROM AdminRevenue ar WHERE ar.createdAt >= :startDate AND ar.createdAt <= :endDate " +
            "GROUP BY ar.fieldName, ar.fieldId ORDER BY SUM(ar.commissionAmount) DESC")
    List<Object[]> getTopPerformingFields(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Hourly Booking Data
    @Query("SELECT EXTRACT(HOUR FROM ar.bookingDate) as hour, COUNT(ar) as bookings, SUM(ar.bookingAmount) as revenue " +
            "FROM AdminRevenue ar WHERE ar.createdAt >= :startDate AND ar.createdAt <= :endDate " +
            "GROUP BY EXTRACT(HOUR FROM ar.bookingDate) ORDER BY EXTRACT(HOUR FROM ar.bookingDate)")
    List<Object[]> getHourlyBookingData(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Revenue by Location
    @Query("SELECT ar.locationName, COUNT(ar) as bookings, SUM(ar.commissionAmount) as commission, SUM(ar.bookingAmount) as revenue " +
            "FROM AdminRevenue ar WHERE ar.createdAt >= :startDate AND ar.createdAt <= :endDate " +
            "GROUP BY ar.locationName ORDER BY SUM(ar.commissionAmount) DESC")
    List<Object[]> getRevenueByLocation(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Top Owners by Revenue
    @Query("SELECT ar.ownerId, COUNT(ar) as bookings, SUM(ar.commissionAmount) as commission, SUM(ar.bookingAmount) as revenue " +
            "FROM AdminRevenue ar WHERE ar.createdAt >= :startDate AND ar.createdAt <= :endDate " +
            "GROUP BY ar.ownerId ORDER BY SUM(ar.bookingAmount) DESC")
    List<Object[]> getTopOwnersByRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Recent Transactions
    List<AdminRevenue> findTop10ByOrderByCreatedAtDesc();
    List<AdminRevenue> findTop20ByOrderByCreatedAtDesc();

    // Revenue by Field ID
    @Query("SELECT SUM(ar.commissionAmount) FROM AdminRevenue ar WHERE ar.fieldId = :fieldId AND ar.createdAt >= :startDate AND ar.createdAt <= :endDate")
    Long getCommissionByFieldAndDateRange(@Param("fieldId") Long fieldId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Revenue by Owner ID
    @Query("SELECT SUM(ar.commissionAmount) FROM AdminRevenue ar WHERE ar.ownerId = :ownerId AND ar.createdAt >= :startDate AND ar.createdAt <= :endDate")
    Long getCommissionByOwnerAndDateRange(@Param("ownerId") Long ownerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Growth Rate Calculation
    @Query("SELECT " +
            "(SELECT SUM(ar1.commissionAmount) FROM AdminRevenue ar1 WHERE ar1.createdAt >= :currentStart AND ar1.createdAt <= :currentEnd) as currentPeriod, " +
            "(SELECT SUM(ar2.commissionAmount) FROM AdminRevenue ar2 WHERE ar2.createdAt >= :previousStart AND ar2.createdAt <= :previousEnd) as previousPeriod")
    Object[] getGrowthRateData(@Param("currentStart") LocalDateTime currentStart, @Param("currentEnd") LocalDateTime currentEnd,
                               @Param("previousStart") LocalDateTime previousStart, @Param("previousEnd") LocalDateTime previousEnd);

    // Find by specific criteria
    List<AdminRevenue> findByFieldIdAndCreatedAtBetween(Long fieldId, LocalDateTime startDate, LocalDateTime endDate);
    List<AdminRevenue> findByOwnerIdAndCreatedAtBetween(Long ownerId, LocalDateTime startDate, LocalDateTime endDate);
    List<AdminRevenue> findByLocationNameAndCreatedAtBetween(String locationName, LocalDateTime startDate, LocalDateTime endDate);
}