package fpt.aptech.management_field.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "field_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long typeId;
    
    private String name;
    
    @Column(name = "team_capacity")
    private Integer teamCapacity;
    
    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @OneToMany(mappedBy = "type")
    @JsonIgnore
    private List<Field> fields;
}