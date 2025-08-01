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
public class ParticipatingTeam {
    @Id
    @Column(name = "participating_team_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long participatingTeamId;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
    
    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @Column(name = "status")
    private String status;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_payment_id", referencedColumnName = "payment_id")
    private Payment entryPayment;
}