package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_review_id")
    private Long locationReviewId;
    
    @Column(name = "average_rating")
    private BigDecimal averageRating;
    
    @Column(name = "total_reviews")
    private Long totalReviews;
    
    @Column(name = "rating_1_count")
    private Long rating1Count = 0L;
    
    @Column(name = "rating_2_count")
    private Long rating2Count = 0L;
    
    @Column(name = "rating_3_count")
    private Long rating3Count = 0L;
    
    @Column(name = "rating_4_count")
    private Long rating4Count = 0L;
    
    @Column(name = "rating_5_count")
    private Long rating5Count = 0L;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
    
    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
    
    @PrePersist
    public void prePersist() {
        this.lastUpdated = LocalDateTime.now();
    }
}