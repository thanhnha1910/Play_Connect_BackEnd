package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.models.Sport;
import fpt.aptech.management_field.payload.dtos.OwnerAnalyticsDto;
import fpt.aptech.management_field.payload.dtos.OwnerSummaryDto;
import fpt.aptech.management_field.payload.dtos.PendingOwnerDto;
import fpt.aptech.management_field.payload.dtos.UserSummaryDto;
import fpt.aptech.management_field.payload.dtos.DetailedAnalyticsDto;
import fpt.aptech.management_field.payload.request.CreateSportRequest;
import fpt.aptech.management_field.payload.request.UpdateSportRequest;
import fpt.aptech.management_field.payload.response.AdminStatsResponse;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.repositories.SportRepository;
import fpt.aptech.management_field.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private SportRepository sportRepository;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            AdminStatsResponse stats = adminService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/locations")
    public ResponseEntity<?> getAllLocations() {
        try {
            // This will need to be implemented based on your Location model
            // List<Location> locations = adminService.getAllLocations();
            // return ResponseEntity.ok(locations);
            return ResponseEntity.ok(new MessageResponse("Locations endpoint - to be implemented"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/owners/pending")
    public ResponseEntity<?> getPendingOwners() {
        try {
            List<PendingOwnerDto> pendingOwners = adminService.getPendingOwnerRequests();
            return ResponseEntity.ok(pendingOwners);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/owners/{userId}/approve")
    public ResponseEntity<?> approveOwner(@PathVariable Long userId) {
        try {
            User approvedOwner = adminService.approveOwner(userId);
            return ResponseEntity.ok(new MessageResponse("Owner approved successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PutMapping("/owners/{userId}/reject")
    public ResponseEntity<?> rejectOwner(@PathVariable Long userId) {
        try {
            User rejectedOwner = adminService.rejectOwner(userId);
            return ResponseEntity.ok(new MessageResponse("Owner rejected successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PutMapping("/owners/{userId}/suspend")
    public ResponseEntity<?> suspendOwner(@PathVariable Long userId) {
        try {
            User suspendedOwner = adminService.suspendOwner(userId);
            return ResponseEntity.ok(new MessageResponse("Owner suspended successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/owners")
    public ResponseEntity<?> getAllOwners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OwnerSummaryDto> owners = adminService.getAllOwners(pageable, search, status);
            return ResponseEntity.ok(owners);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/owners/analytics")
    public ResponseEntity<?> getOwnerAnalytics() {
        try {
            OwnerAnalyticsDto analytics = adminService.getOwnerAnalytics();
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    // User Management Endpoints
    @GetMapping("/users/regular")
    public ResponseEntity<?> getAllRegularUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserSummaryDto> users = adminService.getAllRegularUsers(pageable, search, status);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<?> suspendUser(@PathVariable Long id) {
        try {
            User suspendedUser = adminService.suspendUser(id);
            return ResponseEntity.ok(new MessageResponse("User suspended successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/users/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            User activatedUser = adminService.activateUser(id);
            return ResponseEntity.ok(new MessageResponse("User activated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                     .body(new MessageResponse("Error: " + e.getMessage()));
         }
     }
     
     // Analytics Endpoint
     @GetMapping("/analytics")
     public ResponseEntity<?> getDetailedAnalytics(
             @RequestParam(required = false) String startDate,
             @RequestParam(required = false) String endDate) {
         try {
             LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(3);
             LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
             
             DetailedAnalyticsDto analytics = adminService.getDetailedAnalytics(start, end);
             return ResponseEntity.ok(analytics);
         } catch (Exception e) {
             return ResponseEntity.badRequest()
                     .body(new MessageResponse("Error: " + e.getMessage()));
         }
     }
     
     // Sport Management Endpoints
     @GetMapping("/sports")
     @Operation(summary = "Get all sports (including inactive)", description = "Retrieve all sports for admin management")
     @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Successfully retrieved sports list"),
         @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
     })
     public ResponseEntity<List<Sport>> getAllSports() {
         List<Sport> sports = sportRepository.findAll();
         return ResponseEntity.ok(sports);
     }
 
     @PostMapping("/sports")
     @Operation(summary = "Create new sport", description = "Create a new sport entry")
     @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Sport created successfully"),
         @ApiResponse(responseCode = "400", description = "Invalid input or sport code already exists"),
         @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
     })
     public ResponseEntity<?> createSport(@Valid @RequestBody CreateSportRequest request) {
         try {
             // Check if sport code already exists
             if (sportRepository.existsBySportCode(request.getSportCode())) {
                 return ResponseEntity.badRequest()
                     .body(new MessageResponse("Error: Sport code already exists!"));
             }
 
             Sport sport = new Sport(
                 request.getName(),
                 request.getSportCode(),
                 request.getIcon()
             );
             
             Sport savedSport = sportRepository.save(sport);
             return ResponseEntity.ok(savedSport);
         } catch (Exception e) {
             return ResponseEntity.badRequest()
                 .body(new MessageResponse("Error: " + e.getMessage()));
         }
     }
 
     @PutMapping("/sports/{id}")
     @Operation(summary = "Update sport", description = "Update an existing sport")
     @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Sport updated successfully"),
         @ApiResponse(responseCode = "404", description = "Sport not found"),
         @ApiResponse(responseCode = "400", description = "Invalid input"),
         @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
     })
     public ResponseEntity<?> updateSport(
             @Parameter(description = "Sport ID", required = true)
             @PathVariable Long id,
             @Valid @RequestBody UpdateSportRequest request) {
         try {
             Optional<Sport> sportOptional = sportRepository.findById(id);
             if (!sportOptional.isPresent()) {
                 return ResponseEntity.notFound().build();
             }
 
             Sport sport = sportOptional.get();
             
             // Check if sport code is being changed and if new code already exists
             if (!sport.getSportCode().equals(request.getSportCode()) && 
                 sportRepository.existsBySportCode(request.getSportCode())) {
                 return ResponseEntity.badRequest()
                     .body(new MessageResponse("Error: Sport code already exists!"));
             }
 
             sport.setName(request.getName());
             sport.setSportCode(request.getSportCode());
             sport.setIcon(request.getIcon());
             sport.setIsActive(request.getIsActive());
             
             Sport updatedSport = sportRepository.save(sport);
             return ResponseEntity.ok(updatedSport);
         } catch (Exception e) {
             return ResponseEntity.badRequest()
                 .body(new MessageResponse("Error: " + e.getMessage()));
         }
     }
 
     @DeleteMapping("/sports/{id}")
     @Operation(summary = "Delete sport", description = "Soft delete a sport (set as inactive)")
     @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Sport deleted successfully"),
         @ApiResponse(responseCode = "404", description = "Sport not found"),
         @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
     })
     public ResponseEntity<?> deleteSport(
             @Parameter(description = "Sport ID", required = true)
             @PathVariable Long id) {
         try {
             Optional<Sport> sportOptional = sportRepository.findById(id);
             if (!sportOptional.isPresent()) {
                 return ResponseEntity.notFound().build();
             }
 
             Sport sport = sportOptional.get();
             sport.setIsActive(false);
             sportRepository.save(sport);
             
             return ResponseEntity.ok(new MessageResponse("Sport deleted successfully!"));
         } catch (Exception e) {
             return ResponseEntity.badRequest()
                 .body(new MessageResponse("Error: " + e.getMessage()));
         }
     }
 
     @PostMapping("/sports/{id}/activate")
     @Operation(summary = "Activate sport", description = "Activate a deactivated sport")
     @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Sport activated successfully"),
         @ApiResponse(responseCode = "404", description = "Sport not found"),
         @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
     })
     public ResponseEntity<?> activateSport(
             @Parameter(description = "Sport ID", required = true)
             @PathVariable Long id) {
         try {
             Optional<Sport> sportOptional = sportRepository.findById(id);
             if (!sportOptional.isPresent()) {
                 return ResponseEntity.notFound().build();
             }
 
             Sport sport = sportOptional.get();
             sport.setIsActive(true);
             sportRepository.save(sport);
             
             return ResponseEntity.ok(new MessageResponse("Sport activated successfully!"));
         } catch (Exception e) {
             return ResponseEntity.badRequest()
                 .body(new MessageResponse("Error: " + e.getMessage()));
         }
     }
}