package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.Team;
import fpt.aptech.management_field.models.TeamRoster;
import fpt.aptech.management_field.repositories.TeamRepository;
import fpt.aptech.management_field.repositories.TeamRosterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamRosterRepository teamRosterRepository;

    public List<Team> getTeamsByUserId(Long userId) {
        return teamRepository.findByUserId(userId);
    }

    public Team createTeam(Team team, Long userId) {
        Team newTeam = teamRepository.save(team);
        TeamRoster teamRoster = new TeamRoster(newTeam.getTeamId(), userId, "Captain", true, newTeam, null);
        teamRosterRepository.save(teamRoster);
        return newTeam;
    }

    public Optional<Team> getTeamByName(String name) {
        return teamRepository.findByName(name);
    }

    public Optional<Team> getTeamById(Long id) {
        return teamRepository.findById(id);
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team updateTeam(Team team) {
        return teamRepository.save(team);
    }

    public void deleteTeam(Long id) {
        teamRepository.deleteById(id);
    }
}
