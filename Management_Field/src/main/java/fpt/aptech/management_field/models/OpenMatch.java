package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "open_matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
    @ManyToOne
    @JoinColumn(name = "creator_user_id")
    private User creatorUser;
    
    @Column(name = "sport_type")
    private String sportType;
    
    @Column(name = "slots_needed")
    private Integer slotsNeeded;
    
    @Column(name = "required_tags", columnDefinition = "TEXT")
    private String requiredTags; // JSON string storing list of required tags
    
    @Column(name = "status")
    private String status; // OPEN, FULL, CLOSED
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "OPEN";
        }
    }
}