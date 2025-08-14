package fpt.aptech.management_field.controllers;

    import fpt.aptech.management_field.exception.TokenRefreshException;
    import fpt.aptech.management_field.models.ERole;
    import fpt.aptech.management_field.models.RefreshToken;
    import fpt.aptech.management_field.models.Role;
    import fpt.aptech.management_field.models.User;
    import fpt.aptech.management_field.models.UserStatus;
    import fpt.aptech.management_field.payload.request.ForgotPasswordRequest;
    import fpt.aptech.management_field.payload.request.LoginRequest;
    import fpt.aptech.management_field.payload.request.OAuth2Request;
    import fpt.aptech.management_field.payload.request.OwnerRegistrationRequest;
    import fpt.aptech.management_field.payload.request.ResetPasswordRequest;
    import fpt.aptech.management_field.payload.request.SignupRequest;
    import fpt.aptech.management_field.payload.request.TokenRefreshRequest;
    import fpt.aptech.management_field.payload.response.JwtResponse;
    import fpt.aptech.management_field.payload.response.MessageResponse;
    import fpt.aptech.management_field.payload.response.OAuth2Response;
    import fpt.aptech.management_field.payload.response.TokenRefreshResponse;
    import fpt.aptech.management_field.repositories.RoleRepository;
    import fpt.aptech.management_field.repositories.UserRepository;
    import fpt.aptech.management_field.security.jwt.JwtUtils;
    import fpt.aptech.management_field.security.services.AuthService;
    import fpt.aptech.management_field.security.services.RefreshTokenService;
    import fpt.aptech.management_field.security.services.UserDetailsImpl;
    import fpt.aptech.management_field.services.EmailService;
    import fpt.aptech.management_field.services.OAuth2Service;
    import fpt.aptech.management_field.services.UserService;
    import jakarta.validation.Valid;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.web.bind.annotation.*;

    import java.time.LocalDateTime;
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
        RefreshTokenService refreshTokenService;

        @Autowired
        EmailService emailService;

        @Autowired
        UserService userService;

        @Autowired
        OAuth2Service oauth2Service;
        
        @Autowired
        AuthService authService;

        @GetMapping("/verify-email")
        public ResponseEntity<?> verifyEmail(@RequestParam String token) {
            try {
                Optional<User> verifiedUserOpt = userService.verifyEmail(token);

                if (verifiedUserOpt.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Error: Invalid or expired verification token! Please request a new verification email."));
                }

                User verifiedUser = verifiedUserOpt.get();
                System.out.println("User verification completed. Email verified status: " + verifiedUser.isEmailVerified());

                return ResponseEntity.ok(new MessageResponse("Email verified successfully! You can now login."));
            } catch (Exception e) {
                // Log the exception with more details
                System.err.println("Error verifying email: " + e.getMessage());
                System.err.println("Error class: " + e.getClass().getName());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse("Error: An unexpected error occurred during email verification."));
            }
        }

        @PostMapping("/signin")
        public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
            // Chỉ tìm user bằng email
            Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

            if (userOptional.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email not found!"));
            }

            if (!userOptional.get().isEmailVerified()) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is not verified. Please check your email for verification link."));
            }
            
            // Check user status
            User user = userOptional.get();
            if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Error: Tài khoản đang chờ phê duyệt. Vui lòng chờ Quản trị viên xét duyệt."));
            }
            
            if (user.getStatus() == UserStatus.SUSPENDED) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Error: Tài khoản đã bị tạm ngưng. Vui lòng liên hệ quản trị viên."));
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            // Get user status
            User user1 = userOptional.get();
            String status = user1.getStatus() != null ? user1.getStatus().toString() : "ACTIVE";

            // Create a refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            return ResponseEntity.ok(new JwtResponse(jwt,
                                                     refreshToken.getToken(),
                                                     userDetails.getId(),
                                                     userDetails.getUsername(),
                                                     userDetails.getEmail(),
                                                     userDetails.getFullName(),
                                                     roles,
                                                     status,
                                                     user1.isHasCompletedProfile()));
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
            // Khởi tạo memberLevel và bookingCount
            user.setMemberLevel(0);
            user.setBookingCount(0);

            String token = UUID.randomUUID().toString();
            user.setVerificationToken(token);
            user.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(3)); // Token expires in 3 minutes
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
            user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(3)); // Token expires in 3 minutes
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
                        .body(new MessageResponse("Error: Invalid reset token!"));
            }

            // Check if token has expired
            if (user.getResetPasswordTokenExpiry() != null &&
                LocalDateTime.now().isAfter(user.getResetPasswordTokenExpiry())) {
                // Clear expired token
                user.setResetPasswordToken(null);
                user.setResetPasswordTokenExpiry(null);
                userRepository.save(user);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Reset password token has expired! Please request a new password reset."));
            }

            // Check if new password is same as current password
            if (encoder.matches(password, user.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: New password must be different from current password!"));
            }

            // Update password and clear token
            user.setPassword(encoder.encode(password));
            user.setResetPasswordToken(null);
            user.setResetPasswordTokenExpiry(null);
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

            try {
                Optional<User> userWithTokenOpt = userService.generateVerificationToken(email);

                if (userWithTokenOpt.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Error: Email not found or already verified!"));
                }

                User userWithToken = userWithTokenOpt.get();

                // Send verification email
                String verificationLink = "http://localhost:3000/verify-email?token=" + userWithToken.getVerificationToken();
                String subject = "Email Verification";
                String content = "Please click the link below to verify your email address:\n" + verificationLink;

                emailService.sendEmail(userWithToken.getEmail(), subject, content);

                return ResponseEntity.ok(new MessageResponse("Verification email has been sent. Please check your email."));
            } catch (Exception e) {
                System.err.println("Error resending verification email: " + e.getMessage());
                System.err.println("Error class: " + e.getClass().getName());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse("Error: An unexpected error occurred while resending verification email."));
            }
        }

        @GetMapping("/users")
        public ResponseEntity<?> getAllUsers() {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        }

        // Debug endpoint to verify user directly
        @PostMapping("/debug-verify")
        public ResponseEntity<?> debugVerifyUser(@RequestBody Map<String, String> request) {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Email is required!"));
            }
            
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: User not found!"));
            }
            
            User user = userOptional.get();
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            user.setVerificationTokenExpiry(null);
            userRepository.save(user);
            
            return ResponseEntity.ok(new MessageResponse("User verified successfully!"));
        }

        @PostMapping("/oauth2/{provider}")
        public ResponseEntity<?> oauth2Login(@PathVariable String provider, 
                                           @Valid @RequestBody OAuth2Request request) {
            try {
                OAuth2Service.OAuth2LoginResult result = oauth2Service.processOAuth2Login(request.getCode(), provider);
                User user = result.getUser();
                
                // Check user status before allowing login
                if (user.getStatus() == UserStatus.SUSPENDED) {
                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Error: Tài khoản đã bị tạm ngưng. Vui lòng liên hệ quản trị viên."));
                }
                
                if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Error: Tài khoản đang chờ phê duyệt. Vui lòng chờ Quản trị viên xét duyệt."));
                }
                
                // Convert roles to Set<String>
                Set<String> roleNames = user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(java.util.stream.Collectors.toSet());
                
                // Create response with complete token and user information
                OAuth2Response response = new OAuth2Response();
                response.setMessage("OAuth2 login successful!");
                response.setToken(result.getAccessToken());
                response.setRefreshToken(result.getRefreshToken());
                response.setTokenType("Bearer");
                response.setExpiresIn(86400L); // 24 hours in seconds
                response.setHasCompletedProfile(user.isHasCompletedProfile());
                
                // Set complete user data
                response.setId(user.getId());
                response.setEmail(user.getEmail());
                response.setFullName(user.getFullName());
                response.setUsername(user.getUsername());
                response.setImageUrl(user.getImageUrl());
                response.setRoles(roleNames);
                response.setEmailVerified(user.isEmailVerified());
                response.setActive(user.isActive());
                
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("OAuth2 login failed: " + e.getMessage()));
            }
        }

        @PostMapping("/register-owner")
        public ResponseEntity<?> registerOwner(@Valid @RequestBody OwnerRegistrationRequest ownerRegistrationRequest) {
            try {
                User user = authService.registerOwner(ownerRegistrationRequest);
                
                return ResponseEntity.ok(new MessageResponse("Owner registration submitted successfully! Please wait for admin approval."));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(e.getMessage()));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse("Error: An unexpected error occurred during registration."));
            }
        }

        @PostMapping("/refresh-token")
        public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
            String requestRefreshToken = request.getRefreshToken();

            return refreshTokenService.findByToken(requestRefreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                        String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                        return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                    })
                    .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                            "Refresh token is not in database!"));
        }

        @PostMapping("/fix-users-without-roles")
        public ResponseEntity<?> fixUsersWithoutRoles() {
            try {
                List<User> allUsers = userRepository.findAll();
                int fixedCount = 0;
                
                for (User user : allUsers) {
                    if (user.getRoles() == null || user.getRoles().isEmpty()) {
                        Set<Role> roles = new HashSet<>();
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                        user.setRoles(roles);
                        userRepository.save(user);
                        fixedCount++;
                        System.out.println("Fixed user without roles: " + user.getEmail());
                    }
                }
                
                return ResponseEntity.ok(new MessageResponse("Fixed " + fixedCount + " users without roles."));
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error fixing users: " + e.getMessage()));
            }
        }
    }