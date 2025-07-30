package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.TeamRoster;
import fpt.aptech.management_field.payload.dtos.TeamRosterDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TeamRosterMapper {
    TeamRosterMapper INSTANCE = Mappers.getMapper(TeamRosterMapper.class);

    @Mapping(source = "team.teamId", target = "teamId")
    @Mapping(source = "user.id", target = "userId")
    TeamRosterDto toDto(TeamRoster teamRoster);
}