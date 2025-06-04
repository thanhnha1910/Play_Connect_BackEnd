package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}