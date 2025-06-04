package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    
    @Column(name = "from_time")
    private LocalDateTime fromTime;
    
    @Column(name = "to_time")
    private LocalDateTime toTime;
    
    private Integer slots;
    
    private String status;
    
    @ManyToOne
    @JoinColumn(name = "field_id")
    private Field field;
}