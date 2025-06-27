package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.payload.dtos.LocationReviewDTO;
import fpt.aptech.management_field.services.LocationReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/locationReviews")
@Tag(name = "Location Review Management", description = "APIs for Location Review Management")
public class LocationReviewController {
    @Autowired
    private LocationReviewService locationReviewService;

    @GetMapping("/{slug}")
    public ResponseEntity<List<LocationReviewDTO>> getReviewsByLocationSlug(@PathVariable String slug) {
        List<LocationReviewDTO> locationReviewDTOS = locationReviewService.getReviewsByLocationSlug(slug);
        return ResponseEntity.ok(locationReviewDTOS);
    }
}
