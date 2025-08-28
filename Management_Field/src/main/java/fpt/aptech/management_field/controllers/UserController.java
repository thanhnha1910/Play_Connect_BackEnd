package fpt.aptech.management_field.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.aptech.management_field.models.Notification;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.BookingHistoryDto;
import fpt.aptech.management_field.payload.dtos.SportProfileDto;
import fpt.aptech.management_field.payload.request.ChangePasswordRequest;
import fpt.aptech.management_field.payload.request.OnboardingRequest;
import fpt.aptech.management_field.payload.request.UpdateUserProfileRequest;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.repositories.UserRepository;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import fpt.aptech.management_field.services.BookingService;
import fpt.aptech.management_field.services.NotificationService;
import fpt.aptech.management_field.services.OnboardingService;
import fpt.aptech.management_field.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private OnboardingService onboardingService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("phoneNumber", user.getPhoneNumber());
            response.put("address", user.getAddress());
            response.put("profilePicture", user.getProfilePicture());
            response.put("isDiscoverable", user.getIsDiscoverable());
            
            // Add membership-related fields
            response.put("bookingCount", user.getBookingCount() != null ? user.getBookingCount() : 0);
            response.put("memberLevel", user.getMemberLevel() != null ? user.getMemberLevel() : 1);
            response.put("hasCompletedProfile", user.isHasCompletedProfile());
            response.put("status", user.getStatus());
            response.put("roles", user.getRoles());
            
            // Parse sport profiles from JSON
            if (user.getSportProfiles() != null) {
                try {
                    Map<String, SportProfileDto> sportProfiles = objectMapper.readValue(
                        user.getSportProfiles(), 
                        new TypeReference<Map<String, SportProfileDto>>() {}
                    );
                    response.put("sportProfiles", sportProfiles);
                } catch (JsonProcessingException e) {
                    response.put("sportProfiles", new HashMap<>());
                }
            } else {
                response.put("sportProfiles", new HashMap<>());
            }
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
        }
    }
    
    @PutMapping("/profile")
    // @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, Object> updates) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            if (updates.containsKey("fullName")) {
                user.setFullName((String) updates.get("fullName"));
            }
            
            if (updates.containsKey("phoneNumber")) {
                user.setPhoneNumber((String) updates.get("phoneNumber"));
            }
            
            if (updates.containsKey("address")) {
                user.setAddress((String) updates.get("address"));
            }
            
            if (updates.containsKey("profilePicture")) {
                user.setProfilePicture((String) updates.get("profilePicture"));
            }
            
            if (updates.containsKey("email")) {
                String newEmail = (String) updates.get("email");
                if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                    return ResponseEntity.badRequest().body(new MessageResponse("Email is already in use!"));
                }
                user.setEmail(newEmail);
            }
            
            userRepository.save(user);
            
            return ResponseEntity.ok(new MessageResponse("Profile updated successfully"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
        }
    }
    
    @PostMapping("/profile/sport")
    public ResponseEntity<?> updateSportProfiles(@RequestBody UpdateUserProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            try {
                // Convert sport profiles to JSON string
                if (request.getSportProfiles() != null) {
                    String sportProfilesJson = objectMapper.writeValueAsString(request.getSportProfiles());
                    user.setSportProfiles(sportProfilesJson);
                }
                
                if (request.getIsDiscoverable() != null) {
                    user.setIsDiscoverable(request.getIsDiscoverable());
                }
                
                userRepository.save(user);
                
                return ResponseEntity.ok(new MessageResponse("Sport profiles updated successfully"));
            } catch (JsonProcessingException e) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error processing sport profiles: " + e.getMessage()));
            }
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
        }
    }
    
    @PutMapping("/change-password")
    // @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
        }
        
        User user = userOptional.get();
        
        try {
            userService.changePassword(request, user);
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/profile/onboarding")
    public ResponseEntity<?> completeOnboarding(@RequestBody OnboardingRequest request) {
        System.out.println("DEBUG: Onboarding endpoint called");
        System.out.println("DEBUG: Request received: " + request);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        System.out.println("DEBUG: User ID: " + userDetails.getId());
        
        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("DEBUG: User found: " + user.getEmail());
            
            try {
                System.out.println("DEBUG: Processing onboarding with service");
                onboardingService.processOnboarding(user, request);
                System.out.println("DEBUG: Onboarding processed successfully");
                return ResponseEntity.ok(new MessageResponse("Onboarding completed successfully"));
            } catch (Exception e) {
                System.err.println("DEBUG: Error in onboarding processing: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.badRequest().body(new MessageResponse("Error processing onboarding data: " + e.getMessage()));
            }
        } else {
            System.err.println("DEBUG: User not found for ID: " + userDetails.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
        }
    }

    @GetMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("phoneNumber", user.getPhoneNumber());
            response.put("address", user.getAddress());
            response.put("profilePicture", user.getProfilePicture());
            response.put("active", user.isActive());
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
        }
    }
    
    @PutMapping("/{id}/status")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setActive(!user.isActive());
            userRepository.save(user);
            
            String status = user.isActive() ? "activated" : "deactivated";
            return ResponseEntity.ok(new MessageResponse("User " + status + " successfully"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getUserBookings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            List<BookingHistoryDto> bookings = bookingService.getBookingsForUser(userDetails.getId());
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error fetching bookings: " + e.getMessage()));
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getUserNotifications(@RequestParam(required = false) Boolean isRead) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            List<Notification> notifications = notificationService.getNotificationsForUser(userDetails.getId(), isRead);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error fetching notifications: " + e.getMessage()));
        }
    }

    @GetMapping("/notifications/unread")
    public ResponseEntity<?> getUnreadNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            List<Notification> notifications = notificationService.getUnreadNotificationsForUser(userDetails.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error fetching unread notifications: " + e.getMessage()));
        }
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long id) {
        try {
            notificationService.markNotificationAsRead(id);
            return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error marking notification as read: " + e.getMessage()));
        }
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<?> markAllNotificationsAsRead() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();

            notificationService.markAllNotificationsAsRead(userId);
            return ResponseEntity.ok(new MessageResponse("All notifications marked as read successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error marking notifications as read: " + e.getMessage()));
        }
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<?> getUnreadNotificationCount() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();

            Long unreadCount = notificationService.getUnreadNotificationCount(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error getting unread notification count: " + e.getMessage()));
        }
    }
}