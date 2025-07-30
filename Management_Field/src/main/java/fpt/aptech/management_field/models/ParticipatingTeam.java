package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "participating_teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ParticipatingTeamId.class)
public class ParticipatingTeam {
    @Id
    @Column(name = "team_id")
    private Long teamId;
    
    @Id
    @Column(name = "tournament_id")
    private Long tournamentId;
    
    @ManyToOne
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;
    
    @ManyToOne
    @JoinColumn(name = "tournament_id", insertable = false, updatable = false)
    private Tournament tournament;

    @Column(name = "status")
    private String status;

    @Column(name = "payment_token")
    private String paymentToken;

    
}