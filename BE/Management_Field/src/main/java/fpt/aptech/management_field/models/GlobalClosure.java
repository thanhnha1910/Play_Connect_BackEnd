package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "global_closure")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalClosure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "start_date", unique = true)
    private LocalDateTime startDate;
    
    @Column(name = "end_date", unique = true)
    private LocalDateTime endDate;
    
    private String reason;
    
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
}