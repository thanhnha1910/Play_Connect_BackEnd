package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.OwnerAnalyticsDto;
import fpt.aptech.management_field.payload.dtos.OwnerSummaryDto;
import fpt.aptech.management_field.payload.dtos.PendingOwnerDto;
import fpt.aptech.management_field.payload.response.AdminStatsResponse;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

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
}