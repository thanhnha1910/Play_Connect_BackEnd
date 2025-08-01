package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.FieldType;
import fpt.aptech.management_field.models.Location;
import fpt.aptech.management_field.models.Owner;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.FieldDetailDto;
import fpt.aptech.management_field.payload.dtos.FieldSummaryDto;
import fpt.aptech.management_field.payload.dtos.FieldTypeDto;
import fpt.aptech.management_field.payload.dtos.LocationDto;
import fpt.aptech.management_field.payload.request.UpsertFieldRequest;
import fpt.aptech.management_field.payload.request.UpsertFieldTypeRequest;
import fpt.aptech.management_field.payload.request.UpsertLocationRequest;
import fpt.aptech.management_field.payload.response.FileUploadResponse;
import fpt.aptech.management_field.payload.response.LocationResponse;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.repositories.FieldTypeRepository;
import fpt.aptech.management_field.repositories.LocationRepository;
import fpt.aptech.management_field.repositories.OwnerRepository;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import fpt.aptech.management_field.services.FieldService;
import fpt.aptech.management_field.services.FieldTypeService;
import fpt.aptech.management_field.services.FileUploadService;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/owner")
@PreAuthorize("hasRole('OWNER')")
public class OwnerFieldController {

    @Autowired
    private FieldService fieldService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FieldTypeRepository fieldTypeRepository;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @Autowired
    private FieldTypeService fieldTypeService;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private OwnerRepository ownerRepository;

    @GetMapping("/fields")
    public ResponseEntity<?> getOwnerFields(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<FieldSummaryDto> fields = fieldService.getFieldsForCurrentUser(currentUser);
            return ResponseEntity.ok(fields);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/fields/{id}")
    public ResponseEntity<?> getFieldDetails(@PathVariable Long id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FieldDetailDto field = fieldService.getFieldDetails(id, currentUser);
            return ResponseEntity.ok(field);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/fields")
    public ResponseEntity<?> createField(@Valid @RequestBody UpsertFieldRequest request, 
                                        Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FieldDetailDto createdField = fieldService.createField(request, currentUser);
            return ResponseEntity.ok(createdField);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PutMapping("/fields/{id}")
    public ResponseEntity<?> updateField(@PathVariable Long id, 
                                        @Valid @RequestBody UpsertFieldRequest request,
                                        Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FieldDetailDto updatedField = fieldService.updateField(id, request, currentUser);
            return ResponseEntity.ok(updatedField);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @DeleteMapping("/fields/{id}")
    public ResponseEntity<?> deleteField(@PathVariable Long id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            fieldService.deleteField(id, currentUser);
            return ResponseEntity.ok(new MessageResponse("Field deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/locations")
    public ResponseEntity<?> getOwnerLocations(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<LocationDto> locations = fieldService.getOwnerLocations(currentUser);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/locations/{locationId}")
    public ResponseEntity<?> getLocationById(@PathVariable Long locationId, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Location location = locationRepository.findById(locationId)
                    .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));
            
            // Check if the location belongs to the current owner
            if (!location.getOwner().getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("You don't have permission to access this location"));
            }
            
            LocationResponse response = convertToLocationResponse(location);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/locations/{locationId}/fields")
    public ResponseEntity<?> getFieldsByLocation(
            @PathVariable Long locationId,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<FieldSummaryDto> fields = fieldService.getFieldsByLocation(locationId, currentUser);
            return ResponseEntity.ok(fields);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    // DEPRECATED: Global field types endpoint - use location-specific endpoints instead
    // @GetMapping("/field-types")
    // public ResponseEntity<?> getFieldTypes() {
    //     return ResponseEntity.badRequest()
    //             .body(new MessageResponse("This endpoint is deprecated. Use /locations/{locationId}/field-types instead."));
    // }
    
    // Location-specific Field Types endpoints
    @GetMapping("/locations/{locationId}/field-types")
    public ResponseEntity<?> getLocationFieldTypes(@PathVariable Long locationId, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<FieldTypeDto> fieldTypes = fieldTypeService.getFieldTypesByLocation(locationId, currentUser);
            return ResponseEntity.ok(fieldTypes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/locations/{locationId}/field-types")
    public ResponseEntity<?> createFieldType(@PathVariable Long locationId,
                                            @Valid @RequestBody UpsertFieldTypeRequest request,
                                            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FieldTypeDto createdFieldType = fieldTypeService.createFieldType(locationId, request, currentUser);
            return ResponseEntity.ok(createdFieldType);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/field-types/{fieldTypeId}")
    public ResponseEntity<?> updateFieldType(@PathVariable Long fieldTypeId,
                                            @Valid @RequestBody UpsertFieldTypeRequest request,
                                            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FieldTypeDto updatedFieldType = fieldTypeService.updateFieldType(fieldTypeId, request, currentUser);
            return ResponseEntity.ok(updatedFieldType);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/field-types/{fieldTypeId}")
    public ResponseEntity<?> deleteFieldType(@PathVariable Long fieldTypeId, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            fieldTypeService.deleteFieldType(fieldTypeId, currentUser);
            return ResponseEntity.ok(new MessageResponse("Field type deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            List<String> urls = fileUploadService.uploadFiles(files);
            return ResponseEntity.ok(new FileUploadResponse(urls));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error uploading files: " + e.getMessage()));
        }
    }

    @PostMapping("/locations")
    public ResponseEntity<?> createLocation(@Valid @RequestBody UpsertLocationRequest request,
                                           Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Owner owner = ownerRepository.findByUser(currentUser)
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            
            Location location = new Location();
            location.setName(request.getName());
            location.setAddress(request.getAddress());
            location.setLatitude(request.getLatitude());
            location.setLongitude(request.getLongitude());
            location.setThumbnailUrl(request.getThumbnailUrl());
            location.setImageGallery(request.getImageGallery());
            location.setOwner(owner);
            
            Location savedLocation = locationRepository.save(location);
            LocationResponse response = convertToLocationResponse(savedLocation);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error creating location: " + e.getMessage()));
        }
    }

    @PutMapping("/locations/{id}")
    public ResponseEntity<?> updateLocation(@PathVariable Long id,
                                           @Valid @RequestBody UpsertLocationRequest request,
                                           Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Owner owner = ownerRepository.findByUser(currentUser)
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            
            Location location = locationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Location not found"));
            
            // Check if the location belongs to the current owner
            if (!location.getOwner().getOwnerId().equals(owner.getOwnerId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("You don't have permission to update this location"));
            }
            
            location.setName(request.getName());
            location.setAddress(request.getAddress());
            location.setLatitude(request.getLatitude());
            location.setLongitude(request.getLongitude());
            location.setThumbnailUrl(request.getThumbnailUrl());
            location.setImageGallery(request.getImageGallery());
            
            Location savedLocation = locationRepository.save(location);
            LocationResponse response = convertToLocationResponse(savedLocation);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error updating location: " + e.getMessage()));
        }
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<?> deleteLocation(@PathVariable Long id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Owner owner = ownerRepository.findByUser(currentUser)
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            
            Location location = locationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Location not found"));
            
            // Check if the location belongs to the current owner
            if (!location.getOwner().getOwnerId().equals(owner.getOwnerId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("You don't have permission to delete this location"));
            }
            
            locationRepository.delete(location);
            return ResponseEntity.ok(new MessageResponse("Location deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error deleting location: " + e.getMessage()));
        }
    }

    private User getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private LocationResponse convertToLocationResponse(Location location) {
        LocationResponse response = new LocationResponse();
        response.setLocationId(location.getLocationId());
        response.setName(location.getName());
        response.setSlug(location.getSlug());
        response.setAddress(location.getAddress());
        response.setLatitude(location.getLatitude());
        response.setLongitude(location.getLongitude());
        response.setThumbnailUrl(location.getThumbnailUrl());
        response.setImageGallery(location.getImageGallery());
        response.setOwnerId(location.getOwner().getOwnerId());
        return response;
    }
}