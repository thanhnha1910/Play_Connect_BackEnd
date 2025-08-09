// Enhanced Admin Controller with comprehensive dashboard and analytics
package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.AdminRevenue;
import fpt.aptech.management_field.payload.dtos.*;
import fpt.aptech.management_field.payload.response.AdminStatsResponse;
import fpt.aptech.management_field.services.AdminService;
import fpt.aptech.management_field.services.AdminAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminAnalyticsService adminAnalyticsService;

    // ========== DASHBOARD ENDPOINTS ==========

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

    // ========== ANALYTICS ENDPOINTS ==========

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

    // ========== OWNER MANAGEMENT (existing endpoints enhanced) ==========

    @GetMapping("/owners/pending")
    public ResponseEntity<List<PendingOwnerDto>> getPendingOwners() {
        try {
            List<PendingOwnerDto> pendingOwners = adminService.getPendingOwnerRequests();
            return ResponseEntity.ok(pendingOwners);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @PostMapping("/owners/{userId}/approve")
    public ResponseEntity<Map<String, Object>> approveOwner(@PathVariable Long userId) {
        try {
            adminService.approveOwner(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Owner approved successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/owners/{userId}/reject")
    public ResponseEntity<Map<String, Object>> rejectOwner(@PathVariable Long userId) {
        try {
            adminService.rejectOwner(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Owner rejected successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/owners/{userId}/suspend")
    public ResponseEntity<Map<String, Object>> suspendOwner(@PathVariable Long userId) {
        try {
            adminService.suspendOwner(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Owner suspended successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/owners")
    public ResponseEntity<Page<OwnerSummaryDto>> getAllOwners(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        try {
            Page<OwnerSummaryDto> owners = adminService.getAllOwners(pageable, search, status);
            return ResponseEntity.ok(owners);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Page.empty());
        }
    }

    // ========== USER MANAGEMENT ==========

    @GetMapping("/users")
    public ResponseEntity<Page<UserSummaryDto>> getAllUsers(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        try {
            Page<UserSummaryDto> users = adminService.getAllRegularUsers(pageable, search, status);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Page.empty());
        }
    }

    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<Map<String, Object>> suspendUser(@PathVariable Long userId) {
        try {
            adminService.suspendUser(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User suspended successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/users/{userId}/activate")
    public ResponseEntity<Map<String, Object>> activateUser(@PathVariable Long userId) {
        try {
            adminService.activateUser(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User activated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // ========== SYSTEM HEALTH ==========

    @GetMapping("/system/health")
    public ResponseEntity<DashboardWidgetDto.SystemHealth> getSystemHealth() {
        try {
            DashboardWidgetDto.SystemHealth health = adminAnalyticsService.getSystemHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/system/performance")
    public ResponseEntity<Map<String, Object>> getSystemPerformance() {
        try {
            Map<String, Object> performance = adminAnalyticsService.getSystemPerformance();
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get system performance"));
        }
    }
}