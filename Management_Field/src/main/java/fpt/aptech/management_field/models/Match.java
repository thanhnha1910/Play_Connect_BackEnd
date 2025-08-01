package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Remove the ParticipatingTeam relationships entirely
    // and use repository methods to fetch them when needed
    
    // Keep only:
    @Column(name = "team1_id")
    private Long team1Id;
    
    @Column(name = "team2_id")
    private Long team2Id;
    
    // @ManyToOne
    // @JoinColumn(name = "tournament_id")
    // private Tournament tournament;
    
    // Then use service methods to get ParticipatingTeam entities:
    // participatingTeamRepository.findByTeamIdAndTournamentId(team1Id, tournament.getId())
    
    @Column(name = "team1_score")
    private Integer team1Score;
    
    @Column(name = "team2_score")
    private Integer team2Score;
    
    @Column(name = "is_finished")
    private Boolean isFinished;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String type;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @ManyToOne
    @JoinColumn(name = "field_id")
    private Field field;
    
    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;
    
    // Fixed: Use @JoinColumns for composite foreign key
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "team1_id", referencedColumnName = "team_id", insertable = false, updatable = false),
        @JoinColumn(name = "tournament_id", referencedColumnName = "tournament_id", insertable = false, updatable = false)
    })
    private ParticipatingTeam team1;
    
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "team2_id", referencedColumnName = "team_id", insertable = false, updatable = false),
        @JoinColumn(name = "tournament_id", referencedColumnName = "tournament_id", insertable = false, updatable = false)
    })
    private ParticipatingTeam team2;
}