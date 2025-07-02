package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "holiday_closure")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayClosure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "holiday_name")
    private String holidayName;
    
    @Column(unique = true)
    private LocalDate date;
    
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
}