package fpt.aptech.management_field.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "fields")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "field_id")
    private Long fieldId;

    @Column(name = "name")
    private String name;

    @Column(name = "hourly_rate")
    private Integer hourlyRate;
    
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private FieldCategory category;
    
    @ManyToOne
    @JoinColumn(name = "type_id")
    private FieldType type;
    
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "is_active")
    private Boolean isActive;
    
    @PrePersist
    public void prePersist() {
        if (isActive == null) {
            isActive = true;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    // Explicit getter for isActive to ensure proper access
    public Boolean isActive() {
        return isActive;
    }

    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<FieldClosure> fieldClosures;
}