package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    private Sport sport;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    public Tag(String name, Sport sport) {
        this.name = name;
        this.sport = sport;
        this.isActive = true;
    }
}