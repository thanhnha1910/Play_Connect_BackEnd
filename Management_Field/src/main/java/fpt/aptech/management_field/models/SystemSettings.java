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
    
    @Column(name = "setting_key", unique = true, nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String key;
    
    @Column(name = "setting_value", columnDefinition = "NVARCHAR(MAX)")
    private String value;
    
    @Column(name = "setting_type", columnDefinition = "NVARCHAR(MAX)")
    private String type; // BOOLEAN, STRING, INTEGER, DOUBLE
    
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;
}