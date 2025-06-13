package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.payload.response.FieldSummaryResponse;
import fpt.aptech.management_field.payload.response.LocationCardResponse;
import fpt.aptech.management_field.payload.response.LocationMapResponse;
import fpt.aptech.management_field.services.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/locations")
@Tag(name = "Location Management", description = "APIs for location-based field search and discovery")
public class LocationController {
    
    @Autowired
    private LocationService locationService;
    
    @GetMapping
    @Operation(summary = "Get all locations for browsing", description = "Get a list of all football field locations for the main discovery page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all locations"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<LocationCardResponse>> getAllLocations() {
        List<LocationCardResponse> locations = locationService.getAllLocationsForCards();
        return ResponseEntity.ok(locations);
    }
    
    @GetMapping("/map-search")
    @Operation(summary = "Search locations on map", description = "Get a list of locations in a geographic area to display markers on the map")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved locations"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<List<LocationMapResponse>> searchLocationsForMap(
            @Parameter(description = "Latitude coordinate", required = true, example = "10.762622")
            @RequestParam BigDecimal latitude,
            @Parameter(description = "Longitude coordinate", required = true, example = "106.660172")
            @RequestParam BigDecimal longitude,
            @Parameter(description = "Search radius in kilometers", example = "5.0")
            @RequestParam(defaultValue = "5.0") Double radiusKm,
            @Parameter(description = "Map zoom level for clustering")
            @RequestParam(required = false) Integer zoomLevel,
            @Parameter(description = "Filter by field type ID")
            @RequestParam(required = false) Long typeId,
            @Parameter(description = "Filter by field category ID")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Minimum hourly rate filter")
            @RequestParam(required = false) Integer minHourlyRate,
            @Parameter(description = "Maximum hourly rate filter")
            @RequestParam(required = false) Integer maxHourlyRate) {
        
        List<LocationMapResponse> locations = locationService.searchLocationsForMap(
            latitude, longitude, radiusKm, zoomLevel, typeId, categoryId, minHourlyRate, maxHourlyRate
        );
        
        return ResponseEntity.ok(locations);
    }
    
    @GetMapping("/{locationId}/fields")
    @Operation(summary = "Get fields by location", description = "Get a list of fields available at a specific location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved fields"),
        @ApiResponse(responseCode = "404", description = "Location not found")
    })
    public ResponseEntity<List<FieldSummaryResponse>> getFieldsByLocation(
            @Parameter(description = "Location ID", required = true, example = "1")
            @PathVariable Long locationId,
            @Parameter(description = "Filter by field type ID")
            @RequestParam(required = false) Long typeId,
            @Parameter(description = "Filter by field category ID")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Minimum hourly rate filter")
            @RequestParam(required = false) Integer minHourlyRate,
            @Parameter(description = "Maximum hourly rate filter")
            @RequestParam(required = false) Integer maxHourlyRate) {
        
        try {
            List<FieldSummaryResponse> fields = locationService.getFieldsByLocation(
                locationId, typeId, categoryId, minHourlyRate, maxHourlyRate
            );
            return ResponseEntity.ok(fields);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}