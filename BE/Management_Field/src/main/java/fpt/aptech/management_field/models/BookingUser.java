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
    
    @ManyToOne
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;
    
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}