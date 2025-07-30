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
    
    private Integer total;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String method;

    @Column(name = "transaction_id")
    private String transactionId;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
}