package fpt.aptech.management_field.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "bookings"})
    private User user;


    @Column(name = "from_time")
    private Instant fromTime;
    
    @Column(name = "to_time")
    private Instant toTime;
    
    private Integer slots;
    
    @Column(name = "status", columnDefinition = "varchar(255) check (status in ('confirmed', 'pending', 'cancelled'))")
    private String status;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "field_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "bookings"})
    private Field field;

    @Column(name = "payment_token")
    private String paymentToken;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "booking"})
    private List<BookingUser> bookingUsers;
}