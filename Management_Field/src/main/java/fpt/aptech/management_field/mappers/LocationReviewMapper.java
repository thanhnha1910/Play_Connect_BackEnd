package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.LocationReview;
import fpt.aptech.management_field.payload.dtos.LocationReviewDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface LocationReviewMapper {
    LocationReviewMapper INSTANCE = Mappers.getMapper(LocationReviewMapper.class);

    @Mapping(source = "user", target = "user")
    LocationReviewDTO toDto(LocationReview locationReview);

    List<LocationReviewDTO> toDtoList(List<LocationReview> locationReviews);
}
