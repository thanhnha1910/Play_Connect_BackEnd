package fpt.aptech.management_field.models;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_revenues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRevenue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "field_id")
    private Long fieldId;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "commission_amount")
    private Integer commissionAmount;

    @Column(name = "booking_amount")
    private Integer bookingAmount;

    @Column(name = "commission_rate")
    private Double commissionRate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "owner_amount")
    private Integer ownerAmount;
}