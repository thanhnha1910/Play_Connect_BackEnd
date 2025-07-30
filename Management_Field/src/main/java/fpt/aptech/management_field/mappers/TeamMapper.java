package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.Team;
import fpt.aptech.management_field.payload.dtos.TeamDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {TeamRosterMapper.class})
public interface TeamMapper {
    TeamMapper INSTANCE = Mappers.getMapper(TeamMapper.class);

    TeamDto toDto(Team team);
}