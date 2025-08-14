package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.FieldType;
import fpt.aptech.management_field.models.Location;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.FieldTypeDto;
import fpt.aptech.management_field.payload.request.UpsertFieldTypeRequest;
import fpt.aptech.management_field.repositories.FieldTypeRepository;
import fpt.aptech.management_field.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FieldTypeService {
    
    @Autowired
    private FieldTypeRepository fieldTypeRepository;
    
    @Autowired
    private LocationRepository locationRepository;
    
    public List<FieldTypeDto> getFieldTypesByLocation(Long locationId, User currentUser) {
        // Verify location ownership
        Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));
        
        if (!location.getOwner().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access field types for this location");
        }
        
        List<FieldType> fieldTypes = fieldTypeRepository.findByLocationIdAndOwnerId(locationId, currentUser.getId());
        return fieldTypes.stream()
            .map(this::convertToFieldTypeDto)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public FieldTypeDto createFieldType(Long locationId, UpsertFieldTypeRequest request, User currentUser) {
        // Verify location ownership - use JOIN FETCH to avoid LazyInitializationException
        Location location = locationRepository.findByIdWithOwner(locationId)
            .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));
        
        if (!location.getOwner().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to create field types for this location");
        }
        
        // Create new field type
        FieldType fieldType = new FieldType();
        fieldType.setName(request.getName());
        fieldType.setTeamCapacity(request.getTeamCapacity());
        fieldType.setMaxCapacity(request.getMaxCapacity());
        fieldType.setHourlyRate(request.getHourlyRate());
        fieldType.setDescription(request.getDescription());
        fieldType.setLocation(location);
        
        FieldType savedFieldType = fieldTypeRepository.save(fieldType);
        return convertToFieldTypeDto(savedFieldType);
    }
    
    public FieldTypeDto updateFieldType(Long fieldTypeId, UpsertFieldTypeRequest request, User currentUser) {
        // Verify field type ownership
        FieldType fieldType = fieldTypeRepository.findById(fieldTypeId)
            .orElseThrow(() -> new RuntimeException("Field type not found with id: " + fieldTypeId));
        
        if (!fieldTypeRepository.existsByFieldTypeIdAndOwnerId(fieldTypeId, currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to update this field type");
        }
        
        // Update field type
        fieldType.setName(request.getName());
        fieldType.setTeamCapacity(request.getTeamCapacity());
        fieldType.setMaxCapacity(request.getMaxCapacity());
        fieldType.setHourlyRate(request.getHourlyRate());
        fieldType.setDescription(request.getDescription());
        
        FieldType savedFieldType = fieldTypeRepository.save(fieldType);
        return convertToFieldTypeDto(savedFieldType);
    }
    
    public void deleteFieldType(Long fieldTypeId, User currentUser) {
        // Verify field type ownership
        FieldType fieldType = fieldTypeRepository.findById(fieldTypeId)
            .orElseThrow(() -> new RuntimeException("Field type not found with id: " + fieldTypeId));
        
        if (!fieldTypeRepository.existsByFieldTypeIdAndOwnerId(fieldTypeId, currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to delete this field type");
        }
        
        fieldTypeRepository.delete(fieldType);
    }
    
    private FieldTypeDto convertToFieldTypeDto(FieldType fieldType) {
        FieldTypeDto dto = new FieldTypeDto();
        dto.setTypeId(fieldType.getTypeId());
        dto.setName(fieldType.getName());
        dto.setTeamCapacity(fieldType.getTeamCapacity());
        dto.setMaxCapacity(fieldType.getMaxCapacity());
        dto.setHourlyRate(fieldType.getHourlyRate());
        dto.setDescription(fieldType.getDescription());
        dto.setLocationId(fieldType.getLocation().getLocationId());
        dto.setLocationName(fieldType.getLocation().getName());
        return dto;
    }
}