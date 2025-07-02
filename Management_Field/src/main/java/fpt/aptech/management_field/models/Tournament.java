package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tournaments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tournament_id")
    private Long tournamentId;
    
    private String name;
    
    private Integer prize;
    
    @Column(name = "total_participants")
    private Integer totalParticipants;
    
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
}