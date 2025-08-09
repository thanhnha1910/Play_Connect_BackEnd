package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Payment;
import fpt.aptech.management_field.models.PaymentStatus;
import fpt.aptech.management_field.models.PaymentPayable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find by status and date range
    List<Payment> findByStatusAndCreatedAtBetween(PaymentStatus status, LocalDateTime startDate, LocalDateTime endDate);

    // Find by payable type and status
    List<Payment> findByPayableTypeAndStatus(PaymentPayable payableType, PaymentStatus status);

    // Get total commission for admin
    @Query("SELECT SUM(p.adminCommission) FROM Payment p WHERE p.status = :status AND p.createdAt >= :startDate AND p.createdAt <= :endDate")
    Long getTotalAdminCommission(@Param("status") PaymentStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Get total owner amount
    @Query("SELECT SUM(p.ownerAmount) FROM Payment p WHERE p.status = :status AND p.createdAt >= :startDate AND p.createdAt <= :endDate")
    Long getTotalOwnerAmount(@Param("status") PaymentStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Payment statistics
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.createdAt >= :startDate AND p.createdAt <= :endDate")
    Long getPaymentCountByStatus(@Param("status") PaymentStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Daily payment data
    @Query("SELECT DATE(p.createdAt) as date, COUNT(p) as count, SUM(p.total) as total, SUM(p.adminCommission) as commission " +
            "FROM Payment p WHERE p.status = :status AND p.createdAt >= :startDate AND p.createdAt <= :endDate " +
            "GROUP BY DATE(p.createdAt) ORDER BY DATE(p.createdAt)")
    List<Object[]> getDailyPaymentData(@Param("status") PaymentStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Recent successful payments
    List<Payment> findTop10ByStatusOrderByCreatedAtDesc(PaymentStatus status);

    // Find by transaction ID
    Payment findByTransactionId(String transactionId);

    // Get payments by payable ID and type
    List<Payment> findByPayableIdAndPayableType(Long payableId, PaymentPayable payableType);
}