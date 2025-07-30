package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.ParticipatingTeam;
import fpt.aptech.management_field.models.Team;
import fpt.aptech.management_field.models.TeamRoster;
import fpt.aptech.management_field.models.Tournament;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.models.Location;
import fpt.aptech.management_field.payload.dtos.LocationDto;
import fpt.aptech.management_field.payload.dtos.TeamDto;
import fpt.aptech.management_field.payload.dtos.TeamRosterDto;
import fpt.aptech.management_field.payload.dtos.TournamentDto;
import fpt.aptech.management_field.payload.dtos.UserDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TournamentMapper {

    public TournamentDto toDto(Tournament tournament) {
        if (tournament == null) {
            return null;
        }
        TournamentDto dto = new TournamentDto();
        dto.setTournamentId(tournament.getTournamentId());
        dto.setName(tournament.getName());
        dto.setSlug(tournament.getSlug());
        dto.setDescription(tournament.getDescription());
        dto.setStartDate(tournament.getStartDate());
        dto.setEndDate(tournament.getEndDate());
        dto.setPrize(tournament.getPrize());
        dto.setEntryFee(tournament.getEntryFee());
        dto.setSlots(tournament.getSlots());
        dto.setStatus(tournament.getStatus());
        dto.setLocation(toLocationDto(tournament.getLocation()));

        if (tournament.getParticipatingTeams() != null) {
            dto.setParticipatingTeams(tournament.getParticipatingTeams().stream()
                    .map(pt -> pt.getTeam())
                    .map(this::toTeamDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public TeamDto toTeamDto(Team team) {
        if (team == null) {
            return null;
        }
        TeamDto dto = new TeamDto();
        dto.setTeamId(team.getTeamId());
        dto.setName(team.getName());
        dto.setLogo(team.getLogo());
        dto.setDescription(team.getDescription());
        if (team.getTeamRosters() != null) {
            dto.setTeamRosters(team.getTeamRosters().stream()
                    .map(this::toTeamRosterDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public TeamRosterDto toTeamRosterDto(TeamRoster teamRoster) {
        if (teamRoster == null) {
            return null;
        }
        TeamRosterDto dto = new TeamRosterDto();
        dto.setTeamId(teamRoster.getTeamId());
        dto.setUserId(teamRoster.getUserId());
        dto.setPosition(teamRoster.getPosition());
        dto.setIsCaptain(teamRoster.getIsCaptain());
        dto.setUser(toUserDto(teamRoster.getUser()));
        return dto;
    }

    public UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setAvatar(user.getProfilePicture());
        return dto;
    }

    public LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }
        LocationDto dto = new LocationDto();
        dto.setLocationId(location.getLocationId());
        dto.setName(location.getName());
        dto.setAddress(location.getAddress());
        dto.setCity(location.getCity());
        dto.setCountry(location.getCountry());
        return dto;
    }
}
