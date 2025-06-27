package fpt.aptech.management_field.services;

import fpt.aptech.management_field.mappers.LocationReviewMapper;
import fpt.aptech.management_field.payload.dtos.LocationReviewDTO;
import fpt.aptech.management_field.repositories.LocationReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationReviewService {
    @Autowired
    private LocationReviewRepository locationReviewRepository;

    public List<LocationReviewDTO> getReviewsByLocationSlug(String slug) {
        return LocationReviewMapper.listToDTO(locationReviewRepository.findByLocationSlug(slug));
    }
}
