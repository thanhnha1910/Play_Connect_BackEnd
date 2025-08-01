package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "booking_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BookingUserId.class)
public class BookingUser {
    @Id
    @Column(name = "booking_id")
    private Long bookingId;
    
    @Id
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "is_booker")
    private Boolean isBooker;

    @Column(name = "position")
    private String position; // Vị trí chơi (VD: "GK", "DF")

    @ManyToOne
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;
    
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    // Explicit getters and setters for compatibility
    public Long getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Boolean getIsBooker() {
        return isBooker;
    }
    
    public void setIsBooker(Boolean isBooker) {
        this.isBooker = isBooker;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public Booking getBooking() {
        return booking;
    }
    
    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
}