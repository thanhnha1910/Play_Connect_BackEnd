package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "system_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "setting_key", unique = true, nullable = false)
    private String key;
    
    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String value;
    
    @Column(name = "setting_type")
    private String type; // BOOLEAN, STRING, INTEGER, DOUBLE
    
    @Column(name = "description")
    private String description;
}