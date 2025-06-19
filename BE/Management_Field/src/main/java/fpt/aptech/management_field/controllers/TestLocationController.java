package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.Location;
import fpt.aptech.management_field.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestLocationController {
    
    @Autowired
    private LocationRepository locationRepository;
    
    @GetMapping("/location/{slug}")
    public ResponseEntity<String> testLocationBySlug(@PathVariable String slug) {
        System.out.println("TEST: Searching for slug: " + slug);
        
        Location location = locationRepository.getLocationBySlug(slug);
        System.out.println("TEST: Found location: " + (location != null ? location.getName() : "null"));
        
        if (location != null) {
            return ResponseEntity.ok("Found: " + location.getName() + " with slug: " + location.getSlug());
        } else {
            return ResponseEntity.ok("Not found for slug: " + slug);
        }
    }
    
    @GetMapping("/locations/all")
    public ResponseEntity<String> getAllLocations() {
        List<Location> locations = locationRepository.findAll();
        StringBuilder result = new StringBuilder("All locations:\n");
        for (Location loc : locations) {
            result.append("ID: ").append(loc.getLocationId())
                  .append(", Name: ").append(loc.getName())
                  .append(", Slug: ").append(loc.getSlug())
                  .append("\n");
        }
        return ResponseEntity.ok(result.toString());
    }
}