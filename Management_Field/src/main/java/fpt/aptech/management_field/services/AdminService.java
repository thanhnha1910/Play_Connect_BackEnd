package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.ERole;
import fpt.aptech.management_field.models.Location;
import fpt.aptech.management_field.models.Owner;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.models.UserStatus;
import fpt.aptech.management_field.payload.dtos.OwnerAnalyticsDto;
import fpt.aptech.management_field.payload.dtos.OwnerSummaryDto;
import fpt.aptech.management_field.payload.dtos.PendingOwnerDto;
import fpt.aptech.management_field.payload.dtos.RecentActivityDto;
import fpt.aptech.management_field.payload.dtos.UserSummaryDto;
import fpt.aptech.management_field.payload.dtos.DetailedAnalyticsDto;
import fpt.aptech.management_field.payload.response.AdminStatsResponse;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.FieldRepository;
import fpt.aptech.management_field.repositories.LocationRepository;
import fpt.aptech.management_field.repositories.OwnerRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private FieldRepository fieldRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private OwnerRepository ownerRepository;

    public List<User> getPendingOwners() {
        return userRepository.findByRoles_NameAndStatus(ERole.ROLE_OWNER, UserStatus.PENDING_APPROVAL);
    }
    
    public List<PendingOwnerDto> getPendingOwnerRequests() {
        List<User> pendingUsers = userRepository.findByRoles_NameAndStatus(ERole.ROLE_OWNER, UserStatus.PENDING_APPROVAL);
        
        return pendingUsers.stream()
                .map(user -> {
                    // Try to get Owner information
                    Optional<Owner> ownerOpt = ownerRepository.findByUser(user);
                    String businessName = ownerOpt.map(Owner::getBusinessName).orElse("N/A");
                    
                    return new PendingOwnerDto(
                            user.getId(),
                            user.getFullName(),
                            user.getEmail(),
                            businessName,
                            user.getStatus().toString()
                    );
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public User approveOwner(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found!");
        }

        User user = userOptional.get();
        
        // Check if user has OWNER role
        boolean hasOwnerRole = user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_OWNER);
        
        if (!hasOwnerRole) {
            throw new RuntimeException("User is not an owner!");
        }

        if (user.getStatus() != UserStatus.PENDING_APPROVAL) {
            throw new RuntimeException("User is not in pending approval status!");
        }

        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        
        // Send approval notification email using the new dedicated method
        try {
            emailService.sendOwnerApprovalEmail(user);
        } catch (Exception e) {
            // Log error but don't fail the approval process
            System.err.println("Failed to send approval email: " + e.getMessage());
        }
        
        return savedUser;
    }

    @Transactional
    public User suspendOwner(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found!");
        }

        User user = userOptional.get();
        
        // Check if user has OWNER role
        boolean hasOwnerRole = user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_OWNER);
        
        if (!hasOwnerRole) {
            throw new RuntimeException("User is not an owner!");
        }

        user.setStatus(UserStatus.SUSPENDED);
        return userRepository.save(user);
    }

    @Transactional
    public User rejectOwner(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found!");
        }

        User user = userOptional.get();
        
        // Check if user has OWNER role
        boolean hasOwnerRole = user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_OWNER);
        
        if (!hasOwnerRole) {
            throw new RuntimeException("User is not an owner!");
        }

        if (user.getStatus() != UserStatus.PENDING_APPROVAL) {
            throw new RuntimeException("User is not in pending approval status!");
        }

        user.setStatus(UserStatus.REJECTED);
        User savedUser = userRepository.save(user);
        
        // Send rejection notification email
        try {
            String subject = "Yêu cầu đối tác không được chấp thuận - PlayerConnect";
            String content = "Chúng tôi rất tiếc phải thông báo rằng yêu cầu trở thành đối tác của bạn tại PlayerConnect không được chấp thuận.\n\n" +
                           "Nếu bạn có thắc mắc, vui lòng liên hệ với chúng tôi qua email hỗ trợ.\n\n" +
                           "Trân trọng,\nĐội ngũ PlayerConnect";
            emailService.sendEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            // Log error but don't fail the rejection process
            System.err.println("Failed to send rejection email: " + e.getMessage());
        }
        
        return savedUser;
    }

    public AdminStatsResponse getDashboardStats() {
        // Count pending owners
        long pendingOwnersCount = userRepository.countByRoles_NameAndStatus(ERole.ROLE_OWNER, UserStatus.PENDING_APPROVAL);
        
        // Count total users
        long totalUsersCount = userRepository.count();
        
        // Count active owners
        long activeOwnersCount = userRepository.countByRoles_NameAndStatus(ERole.ROLE_OWNER, UserStatus.ACTIVE);
        
        // Count suspended users
        long suspendedUsersCount = userRepository.countByStatus(UserStatus.SUSPENDED);
        
        // Count total fields
        long totalFieldsCount = fieldRepository.count();
        
        // Count successful bookings in last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        long recentBookingsCount = bookingRepository.findAll().stream()
                .filter(booking -> booking.getCreatedAt() != null && booking.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();
        
        return new AdminStatsResponse(
            totalUsersCount,
            activeOwnersCount,
            pendingOwnersCount,
            suspendedUsersCount,
            totalFieldsCount,
            recentBookingsCount
        );
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }
    
    public Page<OwnerSummaryDto> getAllOwners(Pageable pageable, String search, String status) {
        Specification<User> spec = Specification.where(hasOwnerRole());
        
        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and(hasNameOrEmailContaining(search.trim()));
        }
        
        if (status != null && !status.trim().isEmpty()) {
            try {
                UserStatus userStatus = UserStatus.valueOf(status.trim().toUpperCase());
                spec = spec.and(hasStatus(userStatus));
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }
        
        Page<User> users = userRepository.findAll(spec, pageable);
        
        return users.map(user -> {
            Optional<Owner> ownerOpt = ownerRepository.findByUser(user);
            String businessName = ownerOpt.map(Owner::getBusinessName).orElse("N/A");
            
            return new OwnerSummaryDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getStatus().toString(),
                Instant.now(), // You might want to add a createdAt field to User model
                businessName
            );
        });
    }
    
    public OwnerAnalyticsDto getOwnerAnalytics() {
        long totalOwners = userRepository.countByRoles_NameAndStatus(ERole.ROLE_OWNER, UserStatus.ACTIVE) +
                          userRepository.countByRoles_NameAndStatus(ERole.ROLE_OWNER, UserStatus.PENDING_APPROVAL) +
                          userRepository.countByRoles_NameAndStatus(ERole.ROLE_OWNER, UserStatus.SUSPENDED);
        
        long activeOwners = userRepository.countByRoles_NameAndStatus(ERole.ROLE_OWNER, UserStatus.ACTIVE);
        long pendingApprovalCount = userRepository.countByRoles_NameAndStatus(ERole.ROLE_OWNER, UserStatus.PENDING_APPROVAL);
        long suspendedCount = userRepository.countByRoles_NameAndStatus(ERole.ROLE_OWNER, UserStatus.SUSPENDED);
        
        // For now, we'll return basic analytics without recent activities
        // You can extend this later to include recent activities
        return new OwnerAnalyticsDto(totalOwners, activeOwners, pendingApprovalCount, suspendedCount);
    }
    
    // Specification helper methods
    private Specification<User> hasOwnerRole() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(
                root.join("roles").get("name"), 
                ERole.ROLE_OWNER
            );
        };
    }
    
    private Specification<User> hasNameOrEmailContaining(String search) {
        return (root, query, criteriaBuilder) -> {
            String searchPattern = "%" + search.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern)
            );
        };
    }
    
    private Specification<User> hasStatus(UserStatus status) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
    
    // User Management Methods
    public Page<UserSummaryDto> getAllRegularUsers(Pageable pageable, String search, String status) {
        Specification<User> spec = Specification.where(hasUserRole());
        
        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and(hasNameOrEmailContaining(search.trim()));
        }
        
        if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
            try {
                UserStatus userStatus = UserStatus.valueOf(status.trim().toUpperCase());
                spec = spec.and(hasStatus(userStatus));
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }
        
        Page<User> users = userRepository.findAll(spec, pageable);
        
        return users.map(user -> new UserSummaryDto(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getImageUrl() != null ? user.getImageUrl() : user.getProfilePicture(),
            user.getJoinDate(),
            user.getStatus().toString()
        ));
    }
    
    @Transactional
    public User suspendUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found!");
        }

        User user = userOptional.get();
        
        // Check if user has USER role (not ADMIN or OWNER)
        boolean hasUserRole = user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_USER);
        
        if (!hasUserRole) {
            throw new RuntimeException("Can only suspend regular users!");
        }

        user.setStatus(UserStatus.SUSPENDED);
        return userRepository.save(user);
    }
    
    @Transactional
    public User activateUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found!");
        }

        User user = userOptional.get();
        
        // Check if user has USER role (not ADMIN or OWNER)
        boolean hasUserRole = user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_USER);
        
        if (!hasUserRole) {
            throw new RuntimeException("Can only activate regular users!");
        }

        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }
    
    private Specification<User> hasUserRole() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(
                root.join("roles").get("name"), 
                ERole.ROLE_USER
            );
        };
     }
     
     // Analytics Methods
     public DetailedAnalyticsDto getDetailedAnalytics(LocalDate startDate, LocalDate endDate) {
         // User Growth Data
         List<DetailedAnalyticsDto.TimeSeriesData> userGrowth = generateUserGrowthData(startDate, endDate);
         
         // Revenue Trend Data (based on bookings)
         List<DetailedAnalyticsDto.TimeSeriesData> revenueTrend = generateRevenueTrendData(startDate, endDate);
         
         // Top Performing Fields
         List<DetailedAnalyticsDto.TopPerformingItem> topFields = generateTopFieldsData(startDate, endDate);
         
         // Booking Statistics
         DetailedAnalyticsDto.BookingStats bookingStats = generateBookingStats(startDate, endDate);
         
         return new DetailedAnalyticsDto(userGrowth, revenueTrend, topFields, bookingStats);
     }
     
     private List<DetailedAnalyticsDto.TimeSeriesData> generateUserGrowthData(LocalDate startDate, LocalDate endDate) {
         List<DetailedAnalyticsDto.TimeSeriesData> data = new java.util.ArrayList<>();
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         
         LocalDate current = startDate;
         while (!current.isAfter(endDate)) {
             LocalDateTime dayStart = current.atStartOfDay();
             LocalDateTime dayEnd = current.plusDays(1).atStartOfDay();
             
             long userCount = userRepository.findAll().stream()
                 .filter(user -> user.getJoinDate() != null && 
                         !user.getJoinDate().isBefore(dayStart) && 
                         user.getJoinDate().isBefore(dayEnd))
                 .count();
             
             data.add(new DetailedAnalyticsDto.TimeSeriesData(current.format(formatter), userCount));
             current = current.plusDays(1);
         }
         
         return data;
     }
     
     private List<DetailedAnalyticsDto.TimeSeriesData> generateRevenueTrendData(LocalDate startDate, LocalDate endDate) {
         List<DetailedAnalyticsDto.TimeSeriesData> data = new java.util.ArrayList<>();
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         
         LocalDate current = startDate;
         while (!current.isAfter(endDate)) {
             LocalDateTime dayStart = current.atStartOfDay();
             LocalDateTime dayEnd = current.plusDays(1).atStartOfDay();
             
             // Simulate revenue data based on bookings (you can implement actual payment calculation)
             long dailyRevenue = bookingRepository.findAll().stream()
                 .filter(booking -> booking.getCreatedAt() != null && 
                         !booking.getCreatedAt().isBefore(dayStart) && 
                         booking.getCreatedAt().isBefore(dayEnd))
                 .count() * 50; // Assuming average booking value of 50
             
             data.add(new DetailedAnalyticsDto.TimeSeriesData(current.format(formatter), dailyRevenue));
             current = current.plusDays(1);
         }
         
         return data;
     }
     
     private List<DetailedAnalyticsDto.TopPerformingItem> generateTopFieldsData(LocalDate startDate, LocalDate endDate) {
         LocalDateTime start = startDate.atStartOfDay();
         LocalDateTime end = endDate.plusDays(1).atStartOfDay();
         
         Map<String, Long> fieldBookings = new HashMap<>();
         
         bookingRepository.findAll().stream()
             .filter(booking -> booking.getCreatedAt() != null && 
                     !booking.getCreatedAt().isBefore(start) && 
                     booking.getCreatedAt().isBefore(end))
             .forEach(booking -> {
                 String fieldName = booking.getField() != null ? booking.getField().getName() : "Unknown Field";
                 fieldBookings.merge(fieldName, 1L, Long::sum);
             });
         
         return fieldBookings.entrySet().stream()
             .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
             .limit(10)
             .map(entry -> new DetailedAnalyticsDto.TopPerformingItem(
                 entry.getKey(), 
                 entry.getValue(), 
                 entry.getValue() * 50.0)) // Assuming average revenue per booking
             .collect(java.util.stream.Collectors.toList());
     }
     
     private DetailedAnalyticsDto.BookingStats generateBookingStats(LocalDate startDate, LocalDate endDate) {
         LocalDateTime start = startDate.atStartOfDay();
         LocalDateTime end = endDate.plusDays(1).atStartOfDay();
         
         List<fpt.aptech.management_field.models.Booking> bookings = bookingRepository.findAll().stream()
             .filter(booking -> booking.getCreatedAt() != null && 
                     !booking.getCreatedAt().isBefore(start) && 
                     booking.getCreatedAt().isBefore(end))
             .collect(java.util.stream.Collectors.toList());
         
         long totalBookings = bookings.size();
         double totalRevenue = totalBookings * 50.0; // Assuming average booking value
         double averageBookingValue = totalBookings > 0 ? totalRevenue / totalBookings : 0;
         
         // Generate hourly booking data
         Map<Integer, Long> hourlyBookings = new HashMap<>();
         for (int hour = 0; hour < 24; hour++) {
             hourlyBookings.put(hour, 0L);
         }
         
         bookings.forEach(booking -> {
             if (booking.getCreatedAt() != null) {
                 int hour = booking.getCreatedAt().getHour();
                 hourlyBookings.merge(hour, 1L, Long::sum);
             }
         });
         
         List<DetailedAnalyticsDto.HourlyBookingData> peakHours = hourlyBookings.entrySet().stream()
             .map(entry -> new DetailedAnalyticsDto.HourlyBookingData(entry.getKey(), entry.getValue()))
             .collect(java.util.stream.Collectors.toList());
         
         return new DetailedAnalyticsDto.BookingStats(totalBookings, totalRevenue, averageBookingValue, peakHours);
     }
}