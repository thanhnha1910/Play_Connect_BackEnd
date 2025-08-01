package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "tags", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"name", "sport_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sport sport;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    public Tag(String name, Sport sport) {
        this.name = name;
        this.sport = sport;
        this.isActive = true;
    }
}