package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.models.FieldType;
import fpt.aptech.management_field.models.Location;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.FieldDetailDto;
import fpt.aptech.management_field.payload.dtos.FieldSummaryDto;
import fpt.aptech.management_field.payload.dtos.LocationDto;
import fpt.aptech.management_field.payload.request.UpsertFieldRequest;
import fpt.aptech.management_field.payload.response.FieldDetailResponse;
import fpt.aptech.management_field.payload.response.FieldMapResponse;
import fpt.aptech.management_field.repositories.FieldRepository;
import fpt.aptech.management_field.repositories.FieldTypeRepository;
import fpt.aptech.management_field.repositories.LocationRepository;
import fpt.aptech.management_field.repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
    private LocationRepository locationRepository;
    
    @Autowired
    private FieldTypeRepository fieldTypeRepository;
    
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
    
    // Owner Field Management Methods
    
    public List<FieldSummaryDto> getFieldsForCurrentUser(User currentUser) {
        List<Field> fields = fieldRepository.findByLocation_Owner_User_Id(currentUser.getId());
        return fields.stream()
            .map(this::convertToFieldSummaryDto)
            .collect(Collectors.toList());
    }
    
    public FieldDetailDto getFieldDetails(Long fieldId, User currentUser) {
        Field field = fieldRepository.findById(fieldId)
            .orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));
        
        // Verify ownership
        if (!fieldRepository.existsByFieldIdAndOwnerUserId(fieldId, currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access this field");
        }
        
        return convertToFieldDetailDto(field);
    }
    
    public FieldDetailDto createField(UpsertFieldRequest request, User currentUser) {
        // Verify that the location belongs to the current user
        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new RuntimeException("Location not found with id: " + request.getLocationId()));
        
        if (!location.getOwner().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to create fields in this location");
        }
        
        // Verify field type exists and belongs to the location
        FieldType fieldType = fieldTypeRepository.findById(request.getTypeId())
            .orElseThrow(() -> new RuntimeException("Field type not found with id: " + request.getTypeId()));
        
        if (!fieldType.getLocation().getLocationId().equals(request.getLocationId())) {
            throw new RuntimeException("Field type does not belong to the specified location");
        }
        
        // Create new field
        Field field = new Field();
        field.setName(request.getName());
        field.setDescription(request.getDescription());
        field.setType(fieldType);
        field.setLocation(location);
        field.setIsActive(request.getIsActive());
        field.setHourlyRate(request.getHourlyRate());
        field.setThumbnailUrl(request.getThumbnailUrl());
        field.setImageGallery(request.getImageGallery());
        
        Field savedField = fieldRepository.save(field);
        return convertToFieldDetailDto(savedField);
    }
    
    public FieldDetailDto updateField(Long fieldId, UpsertFieldRequest request, User currentUser) {
        Field field = fieldRepository.findById(fieldId)
            .orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));
        
        // Verify ownership of the field
        if (!fieldRepository.existsByFieldIdAndOwnerUserId(fieldId, currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to update this field");
        }
        
        // If location is being changed, verify ownership of the new location
        if (!field.getLocation().getLocationId().equals(request.getLocationId())) {
            Location newLocation = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + request.getLocationId()));
            
            if (!newLocation.getOwner().getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You don't have permission to move fields to this location");
            }
            
            field.setLocation(newLocation);
        }
        
        // Verify field type exists and belongs to the location
        FieldType fieldType = fieldTypeRepository.findById(request.getTypeId())
            .orElseThrow(() -> new RuntimeException("Field type not found with id: " + request.getTypeId()));
        
        if (!fieldType.getLocation().getLocationId().equals(request.getLocationId())) {
            throw new RuntimeException("Field type does not belong to the specified location");
        }
        
        // Update field properties
        field.setName(request.getName());
        field.setDescription(request.getDescription());
        field.setType(fieldType);
        field.setIsActive(request.getIsActive());
        field.setHourlyRate(request.getHourlyRate());
        field.setThumbnailUrl(request.getThumbnailUrl());
        field.setImageGallery(request.getImageGallery());
        
        Field updatedField = fieldRepository.save(field);
        return convertToFieldDetailDto(updatedField);
    }
    
    public void deleteField(Long fieldId, User currentUser) {
        Field field = fieldRepository.findById(fieldId)
            .orElseThrow(() -> new RuntimeException("Field not found with id: " + fieldId));
        
        // Verify ownership
        if (!fieldRepository.existsByFieldIdAndOwnerUserId(fieldId, currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to delete this field");
        }
        
        fieldRepository.delete(field);
    }
    
    public List<LocationDto> getOwnerLocations(User currentUser) {
        List<Location> locations = locationRepository.findByOwner_User_Id(currentUser.getId());
        return locations.stream()
            .map(this::convertToLocationDto)
            .collect(Collectors.toList());
    }
    
    public List<FieldSummaryDto> getFieldsByLocation(Long locationId, User currentUser) {
        // Verify location ownership
        Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));
        
        if (!location.getOwner().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access fields for this location");
        }
        
        List<Field> fields = fieldRepository.findByLocation_LocationId(locationId);
        return fields.stream()
            .map(this::convertToFieldSummaryDto)
            .collect(Collectors.toList());
    }
    
    // Helper conversion methods
    
    private FieldSummaryDto convertToFieldSummaryDto(Field field) {
        FieldSummaryDto dto = new FieldSummaryDto();
        dto.setFieldId(field.getFieldId());
        dto.setName(field.getName());
        dto.setFieldTypeName(field.getType().getName());
        dto.setLocationName(field.getLocation().getName());
        dto.setIsActive(field.getIsActive());
        dto.setHourlyRate(field.getHourlyRate());
        return dto;
    }
    
    private FieldDetailDto convertToFieldDetailDto(Field field) {
        FieldDetailDto dto = new FieldDetailDto();
        dto.setFieldId(field.getFieldId());
        dto.setName(field.getName());
        dto.setDescription(field.getDescription());
        dto.setFieldTypeName(field.getType().getName());
        dto.setLocationName(field.getLocation().getName());
        dto.setIsActive(field.getIsActive());
        dto.setHourlyRate(field.getHourlyRate());
        dto.setTypeId(field.getType().getTypeId());
        dto.setLocationId(field.getLocation().getLocationId());
        dto.setThumbnailUrl(field.getThumbnailUrl());
        dto.setImageGallery(field.getImageGallery());
        return dto;
    }
    
    private LocationDto convertToLocationDto(Location location) {
        LocationDto dto = new LocationDto();
        dto.setLocationId(location.getLocationId());
        dto.setName(location.getName());
        dto.setAddress(location.getAddress());
        return dto;
    }
}