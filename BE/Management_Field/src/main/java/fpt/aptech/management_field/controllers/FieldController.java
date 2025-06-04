package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.payload.response.FieldDetailResponse;
import fpt.aptech.management_field.payload.response.FieldMapResponse;
import fpt.aptech.management_field.payload.response.GeocodeResponse;
import fpt.aptech.management_field.services.FieldService;
import fpt.aptech.management_field.services.GeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/fields")
public class FieldController {
    
    @Autowired
    private FieldService fieldService;
    
    @Autowired
    private GeocodingService geocodingService;
    
    @GetMapping("/map-search")
    public ResponseEntity<List<FieldMapResponse>> searchFieldsForMap(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm,
            @RequestParam(required = false) Integer zoomLevel,
            @RequestParam(required = false) String bounds,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer minHourlyRate,
            @RequestParam(required = false) Integer maxHourlyRate) {
        
        List<FieldMapResponse> fields = fieldService.searchFieldsForMap(
            latitude, longitude, radiusKm, bounds, typeId, categoryId, minHourlyRate, maxHourlyRate
        );
        
        return ResponseEntity.ok(fields);
    }
    
    @GetMapping("/{fieldId}")
    public ResponseEntity<FieldDetailResponse> getFieldDetails(@PathVariable Long fieldId) {
        FieldDetailResponse fieldDetail = fieldService.getFieldDetails(fieldId);
        return ResponseEntity.ok(fieldDetail);
    }
    
    @GetMapping("/geocode")
    public ResponseEntity<GeocodeResponse> geocodeAddress(@RequestParam String address) {
        GeocodeResponse result = geocodingService.geocodeAddress(address);
        return ResponseEntity.ok(result);
    }
}