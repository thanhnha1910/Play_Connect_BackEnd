package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import fpt.aptech.management_field.models.TeamRoster;

@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long teamId;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String name;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String code;
    
    private String logo;
    
    private String description;
    
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeamRoster> teamRosters;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}