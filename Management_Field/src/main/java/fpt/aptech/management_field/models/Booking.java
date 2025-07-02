package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    @Column(name = "from_time")
    private Instant fromTime;
    
    @Column(name = "to_time")
    private Instant toTime;
    
    private Integer slots;
    
    @Column(name = "status", columnDefinition = "varchar(255) check (status in ('confirmed', 'pending', 'cancelled'))")
    private String status;
    
    @ManyToOne
    @JoinColumn(name = "field_id")
    private Field field;

    @Column(name = "payment_token")
    private String paymentToken;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY)
    private List<BookingUser> bookingUsers;
}