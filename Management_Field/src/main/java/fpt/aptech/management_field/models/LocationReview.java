package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    
    private BigDecimal rating;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String comment;
    
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}