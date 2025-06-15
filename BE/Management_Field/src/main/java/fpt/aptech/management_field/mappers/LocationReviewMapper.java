package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.LocationReview;
import fpt.aptech.management_field.payload.dtos.LocationReviewDTO;
import fpt.aptech.management_field.payload.dtos.UserDTO;

import java.util.ArrayList;
import java.util.List;

public class LocationReviewMapper {
    public static LocationReviewDTO mapToDTO(LocationReview locationReview) {
        UserDTO user = UserMapper.mapToDTO(locationReview.getUser());
        LocationReviewDTO locationReviewDTO = new LocationReviewDTO();
        locationReviewDTO.setRating(locationReview.getRating());
        locationReviewDTO.setComment(locationReview.getComment());
        locationReviewDTO.setUser(user);
        return locationReviewDTO;
    }

    public static List<LocationReviewDTO> listToDTO(List<LocationReview> locationReviews) {
        List<LocationReviewDTO> locationReviewDTOS = new ArrayList<>();
        for (LocationReview locationReview : locationReviews) {
            locationReviewDTOS.add(mapToDTO(locationReview));
        }
        return locationReviewDTOS;
    }
}
