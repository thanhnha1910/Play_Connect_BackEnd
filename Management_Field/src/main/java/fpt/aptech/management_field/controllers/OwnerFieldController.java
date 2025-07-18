package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.FieldType;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.FieldDetailDto;
import fpt.aptech.management_field.payload.dtos.FieldSummaryDto;
import fpt.aptech.management_field.payload.dtos.LocationDto;
import fpt.aptech.management_field.payload.request.UpsertFieldRequest;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.repositories.FieldTypeRepository;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import fpt.aptech.management_field.services.FieldService;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/field-types")
    public ResponseEntity<?> getFieldTypes() {
        try {
            List<FieldType> fieldTypes = fieldTypeRepository.findAll();
            return ResponseEntity.ok(fieldTypes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    private User getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}