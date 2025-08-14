package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.models.AdminRevenue;
import fpt.aptech.management_field.models.Sport;
import fpt.aptech.management_field.payload.dtos.OwnerAnalyticsDto;
import fpt.aptech.management_field.payload.dtos.OwnerSummaryDto;
import fpt.aptech.management_field.payload.dtos.PendingOwnerDto;
import fpt.aptech.management_field.payload.dtos.RevenueAnalyticsDto;
import fpt.aptech.management_field.payload.dtos.UserSummaryDto;
import fpt.aptech.management_field.payload.dtos.AdminAnalyticsDto;
import fpt.aptech.management_field.payload.dtos.DashboardWidgetDto;
import fpt.aptech.management_field.payload.dtos.DetailedAnalyticsDto;
import fpt.aptech.management_field.payload.request.CreateSportRequest;
import fpt.aptech.management_field.payload.request.UpdateSportRequest;
import fpt.aptech.management_field.payload.response.AdminStatsResponse;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.repositories.SportRepository;
import fpt.aptech.management_field.services.AdminAnalyticsService;
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
import org.springframework.format.annotation.DateTimeFormat;
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
    
    @Autowired
    private AdminAnalyticsService adminAnalyticsService;



    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminStatsResponse> getDashboardStats() {
        try {
            AdminStatsResponse stats = adminService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
                      return ResponseEntity.status(500).body(null);

        }
    }

    @GetMapping("/dashboard/widgets")
    public ResponseEntity<DashboardWidgetDto> getDashboardWidgets() {
        try {
            DashboardWidgetDto widgets = adminAnalyticsService.getDashboardWidgets();
            return ResponseEntity.ok(widgets);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
@GetMapping("/dashboard/recent-activities")
    public ResponseEntity<List<DashboardWidgetDto.RecentActivity>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<DashboardWidgetDto.RecentActivity> activities =
                    adminAnalyticsService.getRecentActivities(limit);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }
      @GetMapping("/analytics")
    public ResponseEntity<AdminAnalyticsDto> getAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            AdminAnalyticsDto analytics = adminService.getDetailedAnalytics(startDate, endDate);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
 @GetMapping("/analytics/revenue")
    public ResponseEntity<RevenueAnalyticsDto> getRevenueAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            RevenueAnalyticsDto revenue = adminAnalyticsService.getRevenueAnalytics(startDate, endDate);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            
            return ResponseEntity.status(500).body(null);
        }
    }
     @GetMapping("/analytics/top-fields")
    public ResponseEntity<List<AdminAnalyticsDto.TopField>> getTopFields(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<AdminAnalyticsDto.TopField> topFields =
                    adminAnalyticsService.getTopPerformingFields(startDate, endDate, limit);
            return ResponseEntity.ok(topFields);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }
@GetMapping("/analytics/hourly-bookings")
    public ResponseEntity<List<AdminAnalyticsDto.HourlyData>> getHourlyBookings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<AdminAnalyticsDto.HourlyData> hourlyData =
                    adminAnalyticsService.getHourlyBookingData(startDate, endDate);
            return ResponseEntity.ok(hourlyData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/analytics/location-performance")
    public ResponseEntity<List<AdminAnalyticsDto.LocationRevenue>> getLocationPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<AdminAnalyticsDto.LocationRevenue> locationData =
                    adminAnalyticsService.getLocationPerformance(startDate, endDate);
            return ResponseEntity.ok(locationData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/analytics/booking-status")
    public ResponseEntity<AdminAnalyticsDto.BookingStatusStats> getBookingStatusStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            AdminAnalyticsDto.BookingStatusStats stats =
                    adminAnalyticsService.getBookingStatusStats(startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // ========== TRANSACTION ENDPOINTS ==========

    @GetMapping("/transactions")
    public ResponseEntity<Page<AdminRevenue>> getTransactions(
            Pageable pageable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search) {
        try {
            Page<AdminRevenue> transactions = adminAnalyticsService.getTransactions(
                    pageable, startDate, endDate, search);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Page.empty());
        }
    }

    @GetMapping("/transactions/recent")
    public ResponseEntity<List<AdminAnalyticsDto.RecentTransaction>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<AdminAnalyticsDto.RecentTransaction> transactions =
                    adminAnalyticsService.getRecentTransactions(limit);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/transactions/export")
    public ResponseEntity<Map<String, Object>> exportTransactions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "csv") String format) {
        try {
            Map<String, Object> exportData = adminAnalyticsService.exportTransactions(
                    startDate, endDate, format);
            return ResponseEntity.ok(exportData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Export failed"));
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
     @GetMapping("/analytics/revenue/daily")
    public ResponseEntity<List<AdminAnalyticsDto.DailyRevenueData>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<AdminAnalyticsDto.DailyRevenueData> dailyRevenue =
                    adminAnalyticsService.getDailyRevenueData(startDate, endDate);
            return ResponseEntity.ok(dailyRevenue);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
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

    @PutMapping("/owners/{userId}/activate")
    public ResponseEntity<?> activateOwner(@PathVariable Long userId) {
        try {
            User activatedOwner = adminService.activateOwner(userId);
            return ResponseEntity.ok(new MessageResponse("Owner activated successfully!"));
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