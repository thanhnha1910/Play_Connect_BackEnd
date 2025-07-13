package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.UserDTO;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.repositories.UserRepository;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
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
private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/chat-list")
    public ResponseEntity<List<UserDTO>> getUsersForChat(@RequestParam Long excludeId) {
        List<UserDTO> users = userService.getAllUsersExcept(excludeId);
        return ResponseEntity.ok(users);
    }


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
    
    @PutMapping("/change-password")
    // @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
        }
        
        User user = userOptional.get();
        
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        String confirmPassword = passwordData.get("confirmPassword");
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Current password is incorrect"));
        }
        
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(new MessageResponse("New passwords do not match"));
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
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
}