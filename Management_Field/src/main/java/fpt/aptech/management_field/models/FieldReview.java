package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "field_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "field_review_id")
    private Long fieldReviewId;
    
    private BigDecimal rating;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String comment;
    
    @ManyToOne
    @JoinColumn(name = "field_id")
    private Field field;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /* ====== Owner reply  ====== */
    @Column(name = "owner_reply", columnDefinition = "NVARCHAR(MAX)")
    private String ownerReply;

    @Column(name = "owner_replied_at")
    private LocalDateTime ownerRepliedAt;


}