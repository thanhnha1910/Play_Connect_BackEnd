package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "operating_hours")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperatingHour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "day_of_week")
    private Integer dayOfWeek;
    
    @ManyToOne
    @JoinColumn(name = "field_id")
    private Field field;
    
    @Column(name = "opening_hour")
    private LocalTime openingHour;
    
    @Column(name = "closing_hour")
    private LocalTime closingHour;
}