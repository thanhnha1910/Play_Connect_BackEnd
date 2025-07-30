package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto toDto(User user);
}