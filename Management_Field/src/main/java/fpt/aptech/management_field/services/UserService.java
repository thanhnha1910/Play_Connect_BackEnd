package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public int calculateLevel(int bookingCount) {
        if (bookingCount >= 100) return 4;
        if (bookingCount >= 50) return 3;
        if (bookingCount >= 20) return 2;
        if (bookingCount >= 10) return 1;
        return 0;
    }

    public int getDiscountPercent(int level) {
        return level * 5; // mỗi cấp giảm 2%
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
}