package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.Review;
import fpt.aptech.management_field.payload.dtos.LocationReviewDTO;
import fpt.aptech.management_field.payload.dtos.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationReviewMapper {
    LocationReviewMapper INSTANCE = Mappers.getMapper(LocationReviewMapper.class);

    @Mapping(source = "rating", target = "rating")
    @Mapping(source = "comment", target = "comment")
    @Mapping(source = "user", target = "user")
    LocationReviewDTO toDto(Review review);

    List<LocationReviewDTO> toDtoList(List<Review> reviews);
    
    // Mapping for User to UserDto
    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phoneNumber", target = "phone")
    @Mapping(source = "address", target = "address")
    UserDto userToUserDto(fpt.aptech.management_field.models.User user);
}
