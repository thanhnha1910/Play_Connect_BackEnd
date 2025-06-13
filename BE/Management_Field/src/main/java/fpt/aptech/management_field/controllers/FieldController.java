package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.payload.response.FieldDetailResponse;
import fpt.aptech.management_field.payload.response.FieldMapResponse;
import fpt.aptech.management_field.payload.response.GeocodeResponse;
import fpt.aptech.management_field.services.FieldService;
import fpt.aptech.management_field.services.GeocodingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/fields")
@Tag(name = "Field Management", description = "APIs for field search and discovery - Tìm kiếm & Khám phá Sân")
public class FieldController {

    @Autowired
    private FieldService fieldService;

    @Autowired
    private GeocodingService geocodingService;

//   @GetMapping("/map-search")
//    @Operation(summary = "Search fields on map", description = "Search for fields based on location and filters")
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "200", description = "Successfully retrieved fields"),
//        @ApiResponse(responseCode = "400", description = "Invalid parameters")
//    })
//    public ResponseEntity<List<FieldMapResponse>> searchFieldsForMap(
//            @Parameter(description = "Latitude coordinate", required = true, example = "10.762622")
//            @RequestParam BigDecimal latitude,
//            @Parameter(description = "Longitude coordinate", required = true, example = "106.660172")
//            @RequestParam BigDecimal longitude,
//            @Parameter(description = "Search radius in kilometers", example = "5.0")
//            @RequestParam(defaultValue = "5.0") Double radiusKm,
//            @Parameter(description = "Map zoom level")
//            @RequestParam(required = false) Integer zoomLevel,
//            @Parameter(description = "Map bounds for viewport search")
//            @RequestParam(required = false) String bounds,
//            @Parameter(description = "Filter by field type ID")
//            @RequestParam(required = false) Long typeId,
//            @Parameter(description = "Filter by field category ID")
//            @RequestParam(required = false) Long categoryId,
//            @Parameter(description = "Minimum hourly rate filter")
//            @RequestParam(required = false) Integer minHourlyRate,
//            @Parameter(description = "Maximum hourly rate filter")
//            @RequestParam(required = false) Integer maxHourlyRate) {
//
//        List<FieldMapResponse> fields = fieldService.searchFieldsForMap(
//            latitude, longitude, radiusKm, bounds, typeId, categoryId, minHourlyRate, maxHourlyRate
//        );
//
//        return ResponseEntity.ok(fields);
//    }

    @GetMapping("/{fieldId}")
    @Operation(summary  = "Get field details", description = "Get detailed information about a specific field")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved field details"),
        @ApiResponse(responseCode = "404", description = "Field not found")
    })
    public ResponseEntity<FieldDetailResponse> getFieldDetails(
            @Parameter(description = "Field ID", required = true, example = "1")
            @PathVariable Long fieldId) {
        FieldDetailResponse fieldDetail = fieldService.getFieldDetails(fieldId);
        return ResponseEntity.ok(fieldDetail);
    }

    @GetMapping("/geocode")
    @Operation(summary = "Geocode address", description = "Convert address to coordinates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully geocoded address"),
        @ApiResponse(responseCode = "400", description = "Invalid address")
    })
    public ResponseEntity<GeocodeResponse> geocodeAddress(
            @Parameter(description = "Address to geocode", required = true, example = "District 1, Ho Chi Minh City")
            @RequestParam String address) {
        GeocodeResponse result = geocodingService.geocodeAddress(address);
        return ResponseEntity.ok(result);
    }
}