package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.FieldType;
import fpt.aptech.management_field.models.Location;
import fpt.aptech.management_field.models.Owner;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.FieldDetailDto;
import fpt.aptech.management_field.payload.dtos.FieldSummaryDto;
import fpt.aptech.management_field.payload.dtos.FieldTypeDto;
import fpt.aptech.management_field.payload.dtos.LocationDto;
import fpt.aptech.management_field.payload.dtos.OwnerBookingDto;
import fpt.aptech.management_field.payload.dtos.OwnerBookingStatsDto;
import fpt.aptech.management_field.payload.dtos.PaginationDto;
import fpt.aptech.management_field.payload.request.UpsertFieldRequest;
import fpt.aptech.management_field.payload.request.UpsertFieldTypeRequest;
import fpt.aptech.management_field.payload.request.UpsertLocationRequest;
import fpt.aptech.management_field.payload.response.FileUploadResponse;
import fpt.aptech.management_field.payload.response.LocationResponse;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.payload.response.OwnerBookingsResponse;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.FieldTypeRepository;
import fpt.aptech.management_field.repositories.LocationRepository;
import fpt.aptech.management_field.repositories.OwnerRepository;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import fpt.aptech.management_field.services.*;
import fpt.aptech.management_field.services.ImageStorageService;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

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
    private ImageStorageService imageStorageService;
    
    @Autowired
    private FieldTypeService fieldTypeService;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private OwnerRepository ownerRepository;
    
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
 
    
    @Autowired
    private BookingRepository bookingRepository;
    

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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional
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
    @Transactional
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
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                       @RequestParam(value = "type", defaultValue = "FIELD") String type) {
        try {
            ImageStorageService.ImageType imageType;
            switch (type.toUpperCase()) {
                case "FACILITY":
                    imageType = ImageStorageService.ImageType.FACILITY;
                    break;
                case "FIELD":
                    imageType = ImageStorageService.ImageType.FIELD;
                    break;
                default:
                    imageType = ImageStorageService.ImageType.FIELD;
            }
            
            List<String> urls = imageStorageService.uploadImages(files, imageType);
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
            location.setDescription(request.getDescription());
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
            location.setDescription(request.getDescription());
            // Chỉ cập nhật latitude và longitude nếu có giá trị mới
            if (request.getLatitude() != null) {
                location.setLatitude(request.getLatitude());
            }
            if (request.getLongitude() != null) {
                location.setLongitude(request.getLongitude());
            }
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

    // ==================== BOOKINGS MANAGEMENT ENDPOINTS ====================
    
    /**
     * Get owner bookings with filters and pagination
     */
    @GetMapping("/bookings")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOwnerBookings(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String bookingType,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Convert string dates to Instant
            Instant startInstant = null;
            Instant endInstant = null;
            
            if (startDate != null && !startDate.isEmpty()) {
                startInstant = LocalDate.parse(startDate).atStartOfDay(ZoneId.systemDefault()).toInstant();
            }
            if (endDate != null && !endDate.isEmpty()) {
                endInstant = LocalDate.parse(endDate).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
            }
            
            // Xử lý bookingType để phân loại upcoming/past
            if ("upcoming".equals(bookingType)) {
                // Chỉ lấy bookings có fromTime > hiện tại
                startInstant = Instant.now();
            } else if ("past".equals(bookingType)) {
                // Chỉ lấy bookings có toTime < hiện tại
                endInstant = Instant.now();
            }
            
            // Create pageable
            Pageable pageable = PageRequest.of(page - 1, limit);
            
            // Get total count for pagination
            long totalBookings = bookingRepository.countOwnerBookings(
                currentUser.getId(), startInstant, endInstant, status, facilityId, searchQuery
            );
            
            // Get bookings using repository method with pagination
            org.springframework.data.domain.Page<Booking> bookingPage = bookingRepository.findOwnerBookingsWithPagination(
                currentUser.getId(), startInstant, endInstant, status, facilityId, searchQuery, pageable
            );
            
            // Convert to DTOs
            List<OwnerBookingDto> bookingDtos = bookingPage.getContent().stream()
                .map(this::convertToOwnerBookingDto)
                .collect(Collectors.toList());
            
            // Create pagination info
            PaginationDto pagination = new PaginationDto(
                (int) totalBookings,
                (int) Math.ceil((double) totalBookings / limit),
                page,
                limit
            );
            
            OwnerBookingsResponse response = new OwnerBookingsResponse(bookingDtos, pagination);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("=== ANALYTICAL DASHBOARD ERROR ===");
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get owner booking statistics
     */
    @GetMapping("/bookings/stats")
    public ResponseEntity<?> getOwnerBookingStats(
            @RequestParam(required = false) Long facilityId,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Map<String, Object> stats = bookingService.getOwnerBookingStats(currentUser.getId(), facilityId);
            
            OwnerBookingStatsDto statsDto = new OwnerBookingStatsDto(
                convertToLong(stats.get("totalBookings")),
                convertToLong(stats.get("upcomingCount")),
                convertToLong(stats.get("pendingCount")),
                convertToDouble(stats.get("thisMonthRevenue"))
            );
            
            return ResponseEntity.ok(statsDto);
        } catch (Exception e) {
            System.out.println("=== ANALYTICAL DASHBOARD ERROR ===");
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Helper method to safely convert Object to Long
     */
    private Long convertToLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    /**
     * Helper method to safely convert Object to Double
     */
    private Double convertToDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Float) return ((Float) value).doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * Update booking status
     */
    @PatchMapping("/bookings/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            String status = request.get("status");
            
            if (status == null || status.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Status is required"));
            }
            
            Booking updatedBooking = bookingService.updateBookingStatus(bookingId, status, currentUser.getId());
            OwnerBookingDto bookingDto = convertToOwnerBookingDto(updatedBooking);
            
            return ResponseEntity.ok(bookingDto);
        } catch (Exception e) {
            System.out.println("=== ANALYTICAL DASHBOARD ERROR ===");
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Get owner facilities (locations)
     */
    @GetMapping("/facilities")
    public ResponseEntity<List<Map<String, Object>>> getOwnerFacilities(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<Location> locations = locationRepository.findByOwner_User_Id(currentUser.getId());
            
            List<Map<String, Object>> response = locations.stream()
                .map(location -> {
                    Map<String, Object> facility = new HashMap<>();
                    facility.put("id", location.getLocationId());
                    facility.put("name", location.getName() != null ? location.getName() : "");
                    facility.put("address", location.getAddress() != null ? location.getAddress() : "");
                    return facility;
                })
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch facilities: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(List.of(errorResponse));
        }
    }

    @GetMapping("/dashboard/operational")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOperationalDashboard(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Calculate today's revenue
            double todayRevenue = bookingRepository.calculateTodayRevenueByOwner(currentUser.getId());
            
            // Get today's bookings count
            long todayConfirmed = bookingRepository.countTodayConfirmedBookingsByOwner(currentUser.getId());
            long todayPending = bookingRepository.countTodayPendingBookingsByOwner(currentUser.getId());
            
            // Get upcoming bookings for today
            Instant now = Instant.now();
            Instant endOfDay = LocalDate.now().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
            List<Booking> upcomingBookings = bookingRepository.findUpcomingBookingsByOwner(
                currentUser.getId(), now, endOfDay);
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            
            // Stats
            Map<String, Object> stats = new HashMap<>();
            Map<String, Object> todayRevenueMap = new HashMap<>();
            todayRevenueMap.put("amount", todayRevenue);
            todayRevenueMap.put("changePercent", 15); // TODO: Calculate actual change
            stats.put("todayRevenue", todayRevenueMap);
            
            Map<String, Object> todayBookingsMap = new HashMap<>();
            todayBookingsMap.put("total", todayConfirmed + todayPending);
            todayBookingsMap.put("confirmed", todayConfirmed);
            todayBookingsMap.put("pending", todayPending);
            stats.put("todayBookings", todayBookingsMap);
            
            stats.put("actionRequiredCount", todayPending); // Pending bookings require action
            response.put("stats", stats);
            
            // Upcoming schedule
            List<Map<String, Object>> upcomingSchedule = upcomingBookings.stream()
                .map(booking -> {
                    Map<String, Object> schedule = new HashMap<>();
                    schedule.put("id", booking.getBookingId());
                    
                    // Format time
                    LocalDateTime fromTime = booking.getFromTime().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    LocalDateTime toTime = booking.getToTime().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    schedule.put("time", fromTime.format(timeFormatter) + "-" + toTime.format(timeFormatter));
                    
                    schedule.put("fieldName", booking.getField().getName());
                    schedule.put("customerName", booking.getUser().getUsername());
                    schedule.put("status", booking.getStatus());
                    return schedule;
                })
                .collect(Collectors.toList());
            response.put("upcomingSchedule", upcomingSchedule);
            
            // Notifications (placeholder)
            List<Map<String, Object>> notifications = new ArrayList<>();
            if (todayPending > 0) {
                Map<String, Object> notification = new HashMap<>();
                notification.put("id", 1);
                notification.put("message", "Bạn có " + todayPending + " đặt sân chờ xác nhận.");
                notification.put("link", "/owner/bookings?status=pending");
                notifications.add(notification);
            }
            response.put("notifications", notifications);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("=== ANALYTICAL DASHBOARD ERROR ===");
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/dashboard/ultimate")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getUltimateDashboard(
            @RequestParam(required = false) String facilityId,
            @RequestParam(defaultValue = "today") String period,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Calculate date range based on period
            LocalDate start, end;
            switch (period) {
                case "this_week":
                    start = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
                    end = LocalDate.now().with(java.time.DayOfWeek.SUNDAY);
                    break;
                case "this_month":
                    start = LocalDate.now().withDayOfMonth(1);
                    end = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
                    break;
                case "this_year":
                    start = LocalDate.now().withDayOfYear(1);
                    end = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
                    break;
                default: // today
                    start = LocalDate.now();
                    end = LocalDate.now();
                    break;
            }
            
            Instant startInstant = start.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstant = end.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
            
            // Calculate KPIs
            double totalRevenue = bookingRepository.calculateRevenueByOwnerAndDateRange(
                currentUser.getId(), startInstant, endInstant);
            long totalBookings = bookingRepository.countConfirmedBookingsByOwnerAndDateRange(
                currentUser.getId(), startInstant, endInstant);
            
            // Mock data for new customers and occupancy rate
            long newCustomers = Math.round(totalBookings * 0.3); // 30% are new customers
            double occupancyRate = Math.min(85.0, totalBookings * 12.5); // Mock calculation
            
            // Generate sparkline data for revenue trend
            List<Map<String, Object>> sparklineData = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                Instant dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant dayEnd = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
                double dayRevenue = bookingRepository.calculateRevenueByOwnerAndDateRange(
                    currentUser.getId(), dayStart, dayEnd);
                
                Map<String, Object> point = new HashMap<>();
                point.put("date", date.toString());
                point.put("revenue", dayRevenue);
                sparklineData.add(point);
            }
            
            // Get revenue trend data
            List<Object[]> revenueTrendData = bookingRepository.getRevenueTrendByOwner(
                currentUser.getId(), startInstant, endInstant);
            List<Map<String, Object>> revenueTrend = revenueTrendData.stream()
                .map(row -> {
                    Map<String, Object> trend = new HashMap<>();
                    trend.put("date", row[0].toString());
                    trend.put("revenue", ((Number) row[1]).doubleValue());
                    return trend;
                })
                .collect(Collectors.toList());
            
            // Get revenue distribution (facilities or fields based on facilityId)
            List<Map<String, Object>> revenueDistribution;
            if ("all".equals(facilityId) || facilityId == null) {
                // Show by facilities
                List<Object[]> facilityData = bookingRepository.getPerformanceByFacility(
                    currentUser.getId(), startInstant, endInstant);
                revenueDistribution = facilityData.stream()
                    .map(row -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", row[0].toString());
                        item.put("value", ((Number) row[1]).doubleValue());
                        return item;
                    })
                    .collect(Collectors.toList());
            } else {
                // Show by fields for specific facility
                List<Object[]> fieldData = bookingRepository.getPerformanceByField(
                    currentUser.getId(), startInstant, endInstant);
                revenueDistribution = fieldData.stream()
                    .map(row -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", row[0].toString());
                        item.put("value", ((Number) row[1]).doubleValue());
                        return item;
                    })
                    .collect(Collectors.toList());
            }
            
            // Get recent bookings
            List<Booking> recentBookings = bookingRepository.findUpcomingBookingsByOwner(
                currentUser.getId(), Instant.now(), endInstant);
            List<Map<String, Object>> recentBookingsList = recentBookings.stream()
                .limit(5)
                .map(booking -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", booking.getBookingId());
                    item.put("customerName", booking.getUser().getUsername());
                    item.put("fieldName", booking.getField().getName());
                    item.put("time", booking.getFromTime().toString());
                    item.put("status", booking.getStatus());
                    // Calculate price from field hourly rate and booking duration
                    long hours = java.time.Duration.between(booking.getFromTime(), booking.getToTime()).toHours();
                    double basePrice = booking.getField().getHourlyRate() * hours;
                    
                    // Apply discount if user has memberLevel
                    double finalPrice = basePrice;
                    if (booking.getUser() != null && booking.getUser().getMemberLevel() != null) {
                        int discountPercent = userService.getDiscountPercent(booking.getUser().getMemberLevel());
                        double discountAmount = basePrice * discountPercent / 100;
                        finalPrice = basePrice - discountAmount;
                    }
                    
                    item.put("amount", finalPrice);
                    return item;
                })
                .collect(Collectors.toList());
            
            // Get top performers (facilities or fields)
            List<Map<String, Object>> topPerformers;
            if ("all".equals(facilityId) || facilityId == null) {
                List<Object[]> facilityPerformance = bookingRepository.getPerformanceByFacility(
                    currentUser.getId(), startInstant, endInstant);
                topPerformers = facilityPerformance.stream()
                    .map(row -> {
                        Map<String, Object> performer = new HashMap<>();
                        performer.put("name", row[0].toString());
                        performer.put("revenue", ((Number) row[1]).doubleValue());
                        performer.put("bookingCount", ((Number) row[2]).longValue());
                        return performer;
                    })
                    .collect(Collectors.toList());
            } else {
                List<Object[]> fieldPerformance = bookingRepository.getPerformanceByField(
                    currentUser.getId(), startInstant, endInstant);
                topPerformers = fieldPerformance.stream()
                    .map(row -> {
                        Map<String, Object> performer = new HashMap<>();
                        performer.put("name", row[0].toString());
                        performer.put("revenue", ((Number) row[1]).doubleValue());
                        performer.put("bookingCount", ((Number) row[2]).longValue());
                        return performer;
                    })
                    .collect(Collectors.toList());
            }
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            
            // KPIs
            Map<String, Object> kpis = new HashMap<>();
            kpis.put("totalRevenue", totalRevenue);
            kpis.put("totalBookings", totalBookings);
            kpis.put("newCustomers", newCustomers);
            kpis.put("occupancyRate", Math.round(occupancyRate * 100.0) / 100.0);
            kpis.put("sparklineData", sparklineData);
            response.put("kpis", kpis);
            
            response.put("revenueTrend", revenueTrend);
            response.put("revenueDistribution", revenueDistribution);
            response.put("recentBookings", recentBookingsList);
            response.put("topPerformers", topPerformers);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("=== ANALYTICAL DASHBOARD ERROR ===");
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/dashboard/analytical")
    public ResponseEntity<?> getAnalyticalDashboard(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String facilityId,
            @RequestParam(defaultValue = "this_month") String period,
            Authentication authentication) {
        try {
            System.out.println("=== ANALYTICAL DASHBOARD API CALLED ===");
            System.out.println("Parameters: startDate=" + startDate + ", endDate=" + endDate + ", facilityId=" + facilityId + ", period=" + period);
            
            User currentUser = getCurrentUser(authentication);
            System.out.println("Current user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");
            
            // Check total bookings for this owner
            long totalBookingsForOwner = bookingRepository.countByOwner(currentUser.getId());
            System.out.println("   - Total bookings for owner: " + totalBookingsForOwner);
            
            // Debug: Check booking statuses for this owner
            List<Booking> allOwnerBookings = bookingRepository.findOwnerBookings(
                currentUser.getId(), null, null, null, null, null);
            System.out.println("   - All bookings breakdown:");
            long confirmedCount = allOwnerBookings.stream().filter(b -> "confirmed".equals(b.getStatus())).count();
            long pendingCount = allOwnerBookings.stream().filter(b -> "pending".equals(b.getStatus())).count();
            long cancelledCount = allOwnerBookings.stream().filter(b -> "cancelled".equals(b.getStatus())).count();
            System.out.println("     * Confirmed: " + confirmedCount);
            System.out.println("     * Pending: " + pendingCount);
            System.out.println("     * Cancelled: " + cancelledCount);
            
            // Show sample booking dates
            if (!allOwnerBookings.isEmpty()) {
                System.out.println("   - Sample booking dates:");
                allOwnerBookings.stream().limit(5).forEach(booking -> {
                    System.out.println("     * Booking ID " + booking.getBookingId() + ": " + 
                        booking.getFromTime() + " (status: " + booking.getStatus() + ")");
                });
            }

            // Calculate date range based on period if startDate/endDate not provided
            LocalDate start, end;
            if (startDate != null && endDate != null) {
                start = LocalDate.parse(startDate);
                end = LocalDate.parse(endDate);
            } else {
                // Calculate based on period
                LocalDate now = LocalDate.now();
                switch (period) {
                    case "today":
                        start = now;
                        end = now;
                        break;
                    case "this_week":
                        start = now.minusDays(now.getDayOfWeek().getValue() - 1); // Monday
                        end = now.plusDays(7 - now.getDayOfWeek().getValue()); // Sunday
                        break;
                    case "this_month":
                        start = now.withDayOfMonth(1);
                        end = now.withDayOfMonth(now.lengthOfMonth()); // Last day of month
                        break;
                    case "this_year":
                        start = now.withDayOfYear(1);
                        end = now.withDayOfYear(now.lengthOfYear()); // Last day of year
                        break;
                    default:
                        start = now.minusDays(30);
                        end = now.plusDays(30); // Include future bookings
                }
            }
            
            // Convert to Instant for repository methods
            Instant startInstant = start.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstant = end.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
            
            System.out.println("   - Calculated date range:");
            System.out.println("     * Start: " + start + " -> " + startInstant);
            System.out.println("     * End: " + end + " -> " + endInstant);
            System.out.println("     * Period: " + period);
            
            // Check confirmed bookings in date range
            long confirmedInRange = bookingRepository.countConfirmedBookingsByOwnerAndDateRange(
                currentUser.getId(), startInstant, endInstant);
            System.out.println("   - Confirmed bookings in range: " + confirmedInRange);
            
            // Check ALL bookings in date range (regardless of status)
            List<Booking> allBookingsInRange = bookingRepository.findOwnerBookings(
                currentUser.getId(), startInstant, endInstant, null, null, null);
            System.out.println("   - ALL bookings in range: " + allBookingsInRange.size());
            
            // Show which bookings fall in the range
            if (!allBookingsInRange.isEmpty()) {
                System.out.println("   - Bookings in date range:");
                allBookingsInRange.forEach(booking -> {
                    System.out.println("     * ID " + booking.getBookingId() + ": " + 
                        booking.getFromTime() + " (status: " + booking.getStatus() + ")");
                });
            }
            
            // Parse facilityId if provided
            Long facilityIdLong = null;
            if (facilityId != null && !facilityId.equals("all")) {
                try {
                    facilityIdLong = Long.parseLong(facilityId);
                } catch (NumberFormatException e) {
                    // Invalid facilityId, ignore
                }
            }
            
            // Calculate KPIs using repository methods (with facility filter if provided)
            double totalRevenue;
            long totalBookings;
            
            if (facilityIdLong != null) {
                // Filter by specific facility
                System.out.println("   - Calculating for specific facility: " + facilityIdLong);
                totalRevenue = bookingRepository.calculateRevenueByFacilityAndDateRange(
                    facilityIdLong, startInstant, endInstant);
                System.out.println("===> Revenue from DB (facility): " + totalRevenue);
                
                totalBookings = bookingRepository.countConfirmedBookingsByFacilityAndDateRange(
                    facilityIdLong, startInstant, endInstant);
                System.out.println("===> Bookings count from DB (facility): " + totalBookings);
            } else {
                // All facilities for owner
                System.out.println("   - Calculating for all owner facilities");
                totalRevenue = bookingRepository.calculateRevenueByOwnerAndDateRange(
                    currentUser.getId(), startInstant, endInstant);
                System.out.println("===> Revenue from DB (owner): " + totalRevenue);
                
                totalBookings = bookingRepository.countConfirmedBookingsByOwnerAndDateRange(
                    currentUser.getId(), startInstant, endInstant);
                System.out.println("===> Bookings count from DB (owner): " + totalBookings);
            }
            
            // Debug: Check calculated values
            System.out.println("   - Total revenue calculated: " + totalRevenue);
            System.out.println("   - Total bookings calculated: " + totalBookings);
            
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
            double avgBookingsPerDay = daysBetween > 0 ? (double) totalBookings / daysBetween : 0;
            
            // Get revenue trend data
            List<Object[]> revenueTrendData;
            if (facilityIdLong != null) {
                revenueTrendData = bookingRepository.getRevenueTrendByFacility(
                    facilityIdLong, startInstant, endInstant);
            } else {
                revenueTrendData = bookingRepository.getRevenueTrendByOwner(
                    currentUser.getId(), startInstant, endInstant);
            }
            
            List<Map<String, Object>> revenueTrend = revenueTrendData.stream()
                    .map(row -> {
                        Map<String, Object> trend = new HashMap<>();
                        trend.put("date", row[0].toString()); // date
                        trend.put("revenue", ((Number) row[1]).doubleValue()); // revenue
                        return trend;
                    })
                    .collect(Collectors.toList());
            
            // Get performance data based on filter
            List<Map<String, Object>> performanceBreakdownData;
            if (facilityIdLong != null) {
                // Show fields within the facility
                List<Object[]> fieldPerformanceData = bookingRepository.getPerformanceByFieldInFacility(
                    facilityIdLong, startInstant, endInstant);
                
                performanceBreakdownData = fieldPerformanceData.stream()
                        .map(row -> {
                            Map<String, Object> field = new HashMap<>();
                            field.put("name", row[0].toString()); // field name
                            field.put("revenue", ((Number) row[1]).doubleValue()); // revenue
                            field.put("bookingCount", ((Number) row[2]).longValue()); // booking count
                            return field;
                        })
                        .collect(Collectors.toList());
            } else {
                // Show facilities
                List<Object[]> facilityPerformanceData = bookingRepository.getPerformanceByFacility(
                    currentUser.getId(), startInstant, endInstant);
                
                performanceBreakdownData = facilityPerformanceData.stream()
                        .map(row -> {
                            Map<String, Object> facility = new HashMap<>();
                            facility.put("name", row[0].toString()); // facility name
                            facility.put("revenue", ((Number) row[1]).doubleValue()); // revenue
                            facility.put("bookingCount", ((Number) row[2]).longValue()); // booking count
                            return facility;
                        })
                        .collect(Collectors.toList());
            }
            
            // Get recent bookings
            List<Booking> recentBookings;
            if (facilityIdLong != null) {
                recentBookings = bookingRepository.findRecentBookingsByFacility(
                    facilityIdLong, PageRequest.of(0, 10));
            } else {
                recentBookings = bookingRepository.findRecentBookingsByOwner(
                    currentUser.getId(), PageRequest.of(0, 10));
            }
            
            List<Map<String, Object>> recentBookingsData = recentBookings.stream()
                    .map(booking -> {
                        Map<String, Object> bookingData = new HashMap<>();
                        bookingData.put("id", booking.getBookingId());
                        bookingData.put("customerName", booking.getUser().getFullName() != null ? 
                            booking.getUser().getFullName() : booking.getUser().getUsername());
                        bookingData.put("fieldName", booking.getField().getName());
                        bookingData.put("status", booking.getStatus());
                        bookingData.put("time", booking.getFromTime().toString());
                        return bookingData;
                    })
                    .collect(Collectors.toList());
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            
            // KPIs
            Map<String, Object> kpis = new HashMap<>();
            kpis.put("totalRevenue", totalRevenue);
            kpis.put("totalBookings", totalBookings);
            kpis.put("avgBookingsPerDay", Math.round(avgBookingsPerDay * 100.0) / 100.0);
            kpis.put("newCustomers", totalBookings > 0 ? Math.round(totalBookings * 0.3) : 0); // Estimate 30% new customers
            kpis.put("occupancyRate", totalBookings > 0 ? Math.min(Math.round(avgBookingsPerDay * 10), 100) : 0); // Estimate occupancy rate
            response.put("kpis", kpis);
            
            // Charts data
            Map<String, Object> charts = new HashMap<>();
            charts.put("revenueTrend", revenueTrend);
            
            // Performance breakdown based on filter
            Map<String, Object> performanceBreakdown = new HashMap<>();
            if (facilityIdLong != null) {
                // When filtering by facility, show fields within that facility
                performanceBreakdown.put("byField", performanceBreakdownData);
                performanceBreakdown.put("byFacility", new java.util.ArrayList<>());
            } else {
                // When showing all data, show facilities
                performanceBreakdown.put("byFacility", performanceBreakdownData);
                performanceBreakdown.put("byField", new java.util.ArrayList<>());
            }
            charts.put("performanceBreakdown", performanceBreakdown);
            response.put("charts", charts);
            
            // Recent bookings
            response.put("recentBookings", recentBookingsData);
            
            // Revenue Distribution for Pie Chart (frontend expects this)
            List<Map<String, Object>> revenueDistribution = performanceBreakdownData.stream()
                    .map(item -> {
                        Map<String, Object> dist = new HashMap<>();
                        dist.put("name", item.get("name"));
                        dist.put("value", item.get("revenue"));
                        return dist;
                    })
                    .collect(Collectors.toList());
            response.put("revenueDistribution", revenueDistribution);
            
            // Top Performers for Ranking Table (frontend expects this)
            List<Map<String, Object>> topPerformers = performanceBreakdownData.stream()
                    .sorted((a, b) -> Double.compare(
                        ((Number) b.get("revenue")).doubleValue(),
                        ((Number) a.get("revenue")).doubleValue()
                    ))
                    .limit(10)
                    .collect(Collectors.toList());
            response.put("topPerformers", topPerformers);
            
            System.out.println("=== ANALYTICAL DASHBOARD RESPONSE ===");
            System.out.println("KPIs: " + response.get("kpis"));
            System.out.println("Charts: " + response.get("charts"));
            System.out.println("Recent Bookings count: " + recentBookingsData.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("=== ANALYTICAL DASHBOARD ERROR ===");
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Convert Booking entity to OwnerBookingDto
     */
    private OwnerBookingDto convertToOwnerBookingDto(Booking booking) {
    OwnerBookingDto dto = new OwnerBookingDto();
    dto.setId(booking.getBookingId());
    dto.setCustomerName(booking.getUser().getFullName() != null ? booking.getUser().getFullName() : booking.getUser().getUsername());
    dto.setStatus(booking.getStatus());
    dto.setStartTime(booking.getFromTime());
    dto.setEndTime(booking.getToTime());
    
    // Tính giá thực tế thay vì set 0.0
    if (booking.getFromTime() != null && booking.getToTime() != null && booking.getField() != null) {
        long hours = java.time.Duration.between(booking.getFromTime(), booking.getToTime()).toHours();
        double basePrice = booking.getField().getHourlyRate() * hours;
        
        // Áp dụng giảm giá theo cấp độ thành viên
        double finalPrice;
        if (booking.getUser() != null) {
            Integer memberLevel = booking.getUser().getMemberLevel();
            int discountPercent = userService.getDiscountPercent(memberLevel != null ? memberLevel : 0);
            double discountAmount = basePrice * discountPercent / 100;
            finalPrice = basePrice - discountAmount;
        } else {
            finalPrice = basePrice;
        }
        
        dto.setPrice(finalPrice);
        // Tính số tiền owner thực nhận (95% của finalPrice, trừ 5% cho admin)
        dto.setOwnerAmount(finalPrice * 0.95);
    } else {
        dto.setPrice(0.0);
        dto.setOwnerAmount(0.0);
    }
    
    return dto;
    }
    
    private User getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private LocationResponse convertToLocationResponse(Location location) {
        LocationResponse response = new LocationResponse();
        response.setLocationId(location.getLocationId());
        response.setName(location.getName() != null ? location.getName() : "");
        response.setSlug(location.getSlug());
        response.setAddress(location.getAddress() != null ? location.getAddress() : "");
        response.setDescription(location.getDescription());
        response.setLatitude(location.getLatitude());
        response.setLongitude(location.getLongitude());
        response.setThumbnailUrl(location.getThumbnailUrl());
        response.setImageGallery(location.getImageGallery());
        response.setOwnerId(location.getOwner().getOwnerId());
        return response;
    }
}