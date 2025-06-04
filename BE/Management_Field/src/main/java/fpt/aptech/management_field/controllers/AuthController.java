package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.ERole;
import fpt.aptech.management_field.models.Role;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.request.ForgotPasswordRequest;
import fpt.aptech.management_field.payload.request.LoginRequest;
import fpt.aptech.management_field.payload.request.ResetPasswordRequest;
import fpt.aptech.management_field.payload.request.SignupRequest;
import fpt.aptech.management_field.payload.response.JwtResponse;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.repositories.RoleRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import fpt.aptech.management_field.security.jwt.JwtUtils;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import fpt.aptech.management_field.services.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;
    
    @Autowired
    EmailService emailService;
    
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid verification token!"));
        }
        
        User user = userOptional.get();
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        
        return ResponseEntity.ok(new MessageResponse("Email verified successfully! You can now login."));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional;
        if (loginRequest.getUsername().contains("@")) {
            userOptional = userRepository.findByEmail(loginRequest.getUsername());
        } else {
            userOptional = userRepository.findByUsername(loginRequest.getUsername());
        }
        
        if (userOptional.isPresent() && !userOptional.get().isEmailVerified()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is not verified. Please check your email for verification link."));
        }
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                                                 userDetails.getId(),
                                                 userDetails.getUsername(),
                                                 userDetails.getEmail(),
                                                 userDetails.getFullName(),
                                                 roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setFullName(signUpRequest.getFullName());
        user.setPhoneNumber(signUpRequest.getPhoneNumber());
        user.setAddress(signUpRequest.getAddress());
        
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setEmailVerified(false);

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "owner":
                        Role modRole = roleRepository.findByName(ERole.ROLE_OWNER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);
        
        // Send verification email
        String verificationLink = "http://localhost:3000/verify-email?token=" + token;
        String subject = "Email Verification";
        String content = "Please click the link below to verify your email address:\n" + verificationLink;
        
        try {
            emailService.sendEmail(user.getEmail(), subject, content);
            return ResponseEntity.ok(new MessageResponse("User registered successfully! Please check your email to verify your account."));
        } catch (Exception e) {
            // Log the error
            System.err.println("Failed to send verification email: " + e.getMessage());
            e.printStackTrace();
            
            // Still return success but with a different message
            return ResponseEntity.ok(new MessageResponse("User registered successfully! Email verification failed. Please contact support."));
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        String email = forgotPasswordRequest.getEmail();
        User user = userRepository.findByEmail(email)
                .orElse(null);
                
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email not found!"));
        }
        
        // Generate reset token
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        userRepository.save(user);
        
        // Send email with reset link
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String content = "Please click the link below to reset your password:\n" + resetLink;
        
        emailService.sendEmail(email, subject, content);
        
        return ResponseEntity.ok(new MessageResponse("Password reset link sent to your email."));
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        String token = resetPasswordRequest.getToken();
        String password = resetPasswordRequest.getPassword();
        String confirmPassword = resetPasswordRequest.getConfirmPassword();
        
        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Passwords do not match!"));
        }
        
        User user = userRepository.findByResetPasswordToken(token)
                .orElse(null);
                
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid or expired token!"));
        }
        
        // Update password and clear token
        user.setPassword(encoder.encode(password));
        user.setResetPasswordToken(null);
        userRepository.save(user);
        
        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully."));
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is required!"));
        }
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email not found!"));
        }
        
        User user = userOptional.get();
        
        if (user.isEmailVerified()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already verified!"));
        }
        
        // Generate new verification token if needed
        if (user.getVerificationToken() == null) {
            user.setVerificationToken(UUID.randomUUID().toString());
            userRepository.save(user);
        }
        
        // Send verification email
        String verificationLink = "http://localhost:3000/verify-email?token=" + user.getVerificationToken();
        String subject = "Email Verification";
        String content = "Please click the link below to verify your email address:\n" + verificationLink;
        
        emailService.sendEmail(user.getEmail(), subject, content);
        
        return ResponseEntity.ok(new MessageResponse("Verification email has been sent. Please check your email."));
    }
    
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}