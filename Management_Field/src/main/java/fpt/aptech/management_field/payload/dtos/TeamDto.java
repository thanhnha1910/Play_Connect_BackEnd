package fpt.aptech.management_field.payload.dtos;

import lombok.Data;

import java.util.List;

@Data
public class TeamDto {
    private Long teamId;
    private String name;
    private String logo;
    private String description;
    private List<TeamRosterDto> teamRosters;
}
