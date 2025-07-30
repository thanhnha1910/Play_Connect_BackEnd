package fpt.aptech.management_field.payload.dtos;

import lombok.Data;

@Data
public class ParticipatingTeamDto {
    private Long teamId;
    private Long tournamentId;
    private String status;
    private String paymentToken;
    private TeamDto team;
}
