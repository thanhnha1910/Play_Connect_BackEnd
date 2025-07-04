package fpt.aptech.management_field.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @OneToMany(mappedBy = "category")
    @JsonIgnore
    private List<Field> fields;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
}