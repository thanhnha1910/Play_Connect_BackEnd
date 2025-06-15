package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}