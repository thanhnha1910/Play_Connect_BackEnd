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
    
    @Column(name = "status", columnDefinition = "nvarchar(max) check (status in ('confirmed', 'pending', 'cancelled'))")
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
    
    // Explicit getters and setters for compatibility
    public Long getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Instant getFromTime() {
        return fromTime;
    }
    
    public void setFromTime(Instant fromTime) {
        this.fromTime = fromTime;
    }
    
    public Instant getToTime() {
        return toTime;
    }
    
    public void setToTime(Instant toTime) {
        this.toTime = toTime;
    }
    
    public String getPaymentToken() {
        return paymentToken;
    }
    
    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }
    
    public Field getField() {
        return field;
    }
    
    public void setField(Field field) {
        this.field = field;
    }
    
    public Integer getSlots() {
        return slots;
    }
    
    public void setSlots(Integer slots) {
        this.slots = slots;
    }
    
    public List<BookingUser> getBookingUsers() {
        return bookingUsers;
    }
    
    public void setBookingUsers(List<BookingUser> bookingUsers) {
        this.bookingUsers = bookingUsers;
    }
}