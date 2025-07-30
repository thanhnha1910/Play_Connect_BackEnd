package fpt.aptech.management_field.payload.dtos;

import lombok.Data;

@Data
public class TeamRosterDto {
    private Long teamId;
    private Long userId;
    private String position;
    private Boolean isCaptain;
    private UserDto user;
}
