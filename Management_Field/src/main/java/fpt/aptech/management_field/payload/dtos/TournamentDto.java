package fpt.aptech.management_field.payload.dtos;

import fpt.aptech.management_field.models.Location;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TournamentDto {
    private Long tournamentId;
    private String name;
    private String slug;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer prize;
    private Integer entryFee;
    private Integer slots;
    private String status;
    private LocationDto location;
    private List<TeamDto> participatingTeams;

    public TournamentDto() {
    }

    public TournamentDto(Long tournamentId, String name, String slug, String description, LocalDateTime startDate, LocalDateTime endDate, Integer prize, Integer entryFee, Integer slots, String status, Location location) {
        this.tournamentId = tournamentId;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.prize = prize;
        this.entryFee = entryFee;
        this.slots = slots;
        this.status = status;
        LocationDto locationDto = new LocationDto();
        locationDto.setLocationId(location.getLocationId());
        locationDto.setName(location.getName());
        locationDto.setAddress(location.getAddress());
        locationDto.setCity(location.getCity());
        locationDto.setCountry(location.getCountry());
        this.location = locationDto;
    }
}
