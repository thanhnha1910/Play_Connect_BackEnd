package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_rosters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TeamRosterId.class)
public class TeamRoster {
    @Id
    @Column(name = "team_id")
    private Long teamId;
    
    @Id
    @Column(name = "user_id")
    private Long userId;
    
    private String position;
    
    @Column(name = "is_captain")
    private Boolean isCaptain;
    
    @ManyToOne
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;
    
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}