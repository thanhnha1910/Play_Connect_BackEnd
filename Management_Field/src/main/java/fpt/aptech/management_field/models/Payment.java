package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "total")
    private Integer total;

    @Column(name = "method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "payable_id")
    private Long payableId;

    @Column(name = "payable_type")
    @Enumerated(EnumType.STRING)
    private PaymentPayable payableType;
    // New fields for admin commission
    @Column(name = "admin_commission")
    private Integer adminCommission; // 5% of total amount

    @Column(name = "owner_amount")
    private Integer ownerAmount; // 95% of total amount

    @Column(name = "commission_rate")
    private Double commissionRate = 0.05; // 5%

    // Helper methods
    public void calculateCommission() {
        if (this.total != null) {
            this.adminCommission = (int) (this.total * this.commissionRate);
            this.ownerAmount = this.total - this.adminCommission;
        }
    }
}

