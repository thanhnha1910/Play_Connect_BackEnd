package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.payload.response.FieldDetailResponse;
import fpt.aptech.management_field.payload.response.FieldMapResponse;
import fpt.aptech.management_field.repositories.FieldRepository;
import fpt.aptech.management_field.repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FieldService {
    
    @Autowired
    private FieldRepository fieldRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    public List<FieldMapResponse> searchFieldsForMap(
            BigDecimal latitude, BigDecimal longitude, Double radiusKm,
            String bounds, Long typeId, Long categoryId, 
            Integer minHourlyRate, Integer maxHourlyRate) {
        
        List<Field> fields;
        
        if (bounds != null && !bounds.isEmpty()) {
            // Parse bounds: "southWestLat,southWestLng,northEastLat,northEastLng"
            String[] boundsArray = bounds.split(",");
            if (boundsArray.length == 4) {
                BigDecimal southWestLat = new BigDecimal(boundsArray[0]);
                BigDecimal southWestLng = new BigDecimal(boundsArray[1]);
                BigDecimal northEastLat = new BigDecimal(boundsArray[2]);
                BigDecimal northEastLng = new BigDecimal(boundsArray[3]);
                
                fields = fieldRepository.findFieldsWithinBounds(
                    southWestLat, southWestLng, northEastLat, northEastLng
                );
            } else {
                fields = fieldRepository.findFieldsWithinRadius(latitude, longitude, radiusKm);
            }
        } else {
            fields = fieldRepository.findFieldsWithinRadius(latitude, longitude, radiusKm);
        }
        
        // Apply additional filters
        return fields.stream()
            .filter(field -> typeId == null || field.getType().getTypeId().equals(typeId))
            .filter(field -> categoryId == null || field.getCategory().getCategoryId().equals(categoryId))
            .filter(field -> minHourlyRate == null || field.getHourlyRate() >= minHourlyRate)
            .filter(field -> maxHourlyRate == null || field.getHourlyRate() <= maxHourlyRate)
            .map(this::convertToMapResponse)
            .collect(Collectors.toList());
    }
    
    public FieldDetailResponse getFieldDetails(Long fieldId) {
        Field field = fieldRepository.findById(fieldId)
            .orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));
        
        return convertToDetailResponse(field);
    }
    
    private FieldMapResponse convertToMapResponse(Field field) {
        FieldMapResponse response = new FieldMapResponse();
        response.setFieldId(field.getFieldId());
        response.setFieldName(field.getLocation().getName() + " - " + field.getDescription());
        response.setLocationName(field.getLocation().getName());
        response.setLatitude(field.getLocation().getLatitude());
        response.setLongitude(field.getLocation().getLongitude());
        response.setAddressShort(field.getLocation().getAddress());
        response.setHourlyRate(field.getHourlyRate());
        response.setTypeId(field.getType().getTypeId());
        response.setTypeName(field.getType().getName());
        response.setCategoryId(field.getCategory().getCategoryId());
        response.setCategoryName(field.getCategory().getName());
        // TODO: Calculate average rating from FieldReviews
        response.setAverageRating(BigDecimal.ZERO);
        return response;
    }
    
    private FieldDetailResponse convertToDetailResponse(Field field) {
        FieldDetailResponse response = new FieldDetailResponse();
        response.setFieldId(field.getFieldId());
        response.setDescription(field.getDescription());
        response.setHourlyRate(field.getHourlyRate());
        response.setCreatedAt(field.getCreatedAt());
        
        // Location info
        FieldDetailResponse.LocationInfo locationInfo = new FieldDetailResponse.LocationInfo();
        locationInfo.setLocationId(field.getLocation().getLocationId());
        locationInfo.setName(field.getLocation().getName());
        locationInfo.setAddress(field.getLocation().getAddress());
        locationInfo.setLatitude(field.getLocation().getLatitude());
        locationInfo.setLongitude(field.getLocation().getLongitude());
        locationInfo.setOwnerId(field.getLocation().getOwner().getOwnerId());
        response.setLocation(locationInfo);
        
        // Type info
        FieldDetailResponse.TypeInfo typeInfo = new FieldDetailResponse.TypeInfo();
        typeInfo.setTypeId(field.getType().getTypeId());
        typeInfo.setName(field.getType().getName());
        typeInfo.setTeamCapacity(field.getType().getTeamCapacity());
        typeInfo.setMaxCapacity(field.getType().getMaxCapacity());
        response.setType(typeInfo);
        
        // Category info
        FieldDetailResponse.CategoryInfo categoryInfo = new FieldDetailResponse.CategoryInfo();
        categoryInfo.setCategoryId(field.getCategory().getCategoryId());
        categoryInfo.setName(field.getCategory().getName());
        categoryInfo.setDescription(field.getCategory().getDescription());
        response.setCategory(categoryInfo);
        
        // TODO: Add operating hours and reviews
        
        return response;
    }
    
    public List<Field> getAvailableFields(Instant fromTime, Instant toTime, Long locationId) {
        List<Field> fields;
        
        // Get fields by location if locationId is provided, otherwise get all fields
        if (locationId != null) {
            fields = fieldRepository.getFieldsByLocationId(locationId);
        } else {
            fields = fieldRepository.findAll();
        }
        
        // Filter out fields that have confirmed or pending bookings overlapping with the requested time
        return fields.stream()
            .filter(field -> !bookingRepository.existsByFieldAndFromTimeLessThanEqualAndToTimeGreaterThanEqual(
                field, toTime, fromTime))
            .collect(Collectors.toList());
    }
    
    public List<Field> getAllFields() {
        return fieldRepository.findAll();
    }
}