package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "field_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;
    
    private String name;
    
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
}