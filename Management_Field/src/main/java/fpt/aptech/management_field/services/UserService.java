package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.request.ChangePasswordRequest;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Verify a user's email using the verification token
     * 
     * @param token The verification token
     * @return The verified user or empty if token is invalid or expired
     */
    @Transactional
    public Optional<User> verifyEmail(String token) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);
        
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }
        
        User user = userOptional.get();
        
        // Check if token has expired
        if (user.getVerificationTokenExpiry() != null && 
            LocalDateTime.now().isAfter(user.getVerificationTokenExpiry())) {
            // Clear expired token
            user.setVerificationToken(null);
            user.setVerificationTokenExpiry(null);
            userRepository.saveAndFlush(user);
            return Optional.empty();
        }
        
        // Mark email as verified and clear token
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        
        // Save and flush changes immediately
        return Optional.of(userRepository.saveAndFlush(user));
    }
    
    /**
     * Generate a new verification token for a user
     * 
     * @param email The user's email
     * @return The user with the new verification token or empty if email not found or already verified
     */
    @Transactional
    public Optional<User> generateVerificationToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }
        
        User user = userOptional.get();
        
        if (user.isEmailVerified()) {
            return Optional.empty();
        }
        
        // Generate new verification token with expiry
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(3)); // Token expires in 3 minutes
        
        return Optional.of(userRepository.saveAndFlush(user));
    }
    public int calculateLevel(int bookingCount) {
        if (bookingCount >= 100) return 4;
        if (bookingCount >= 50) return 3;
        if (bookingCount >= 20) return 2;
        if (bookingCount >= 10) return 1;
        return 0;
    }

    public int getDiscountPercent(Integer level) {
        if (level == null) {
            return 0; // Không có discount nếu level null
        }
        return level * 5; // mỗi cấp giảm 5%
    }
    
    @Transactional
    public void changePassword(ChangePasswordRequest request, User currentUser) {
        // Step 1: Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Step 2: Check if new password is same as current password
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Step 3: Validate new password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        // Step 4: Validate password complexity
        validatePasswordComplexity(request.getNewPassword());

        // Step 5: Encode and save new password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        currentUser.setPassword(encodedPassword);
        userRepository.save(currentUser);

        // Step 6: TODO - Invalidate existing refresh tokens for enhanced security
        // This would require implementing a token blacklist or refresh token repository
    }

    private void validatePasswordComplexity(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        
        // Add more complexity requirements as needed
        // Example: require at least one uppercase, one lowercase, one digit
        // if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
        //     throw new IllegalArgumentException("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
        // }
    }
}