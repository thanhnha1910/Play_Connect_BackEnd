package fpt.aptech.management_field.security.services;

import fpt.aptech.management_field.models.RefreshToken;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.repositories.RefreshTokenRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import fpt.aptech.management_field.exception.TokenRefreshException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${jwt.refreshExpirationMs:604800000}")
    private Long refreshTokenDurationMs;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    /**
     * Creates a new refresh token for the user.
     * Implements "Clean then Create" strategy to handle unique constraint on user_id.
     * This ensures only one active refresh token per user at any time.
     * 
     * @param userId The ID of the user
     * @return The newly created refresh token
     */
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Step 1: Clean - Delete any existing refresh tokens for this user
        // This prevents DataIntegrityViolationException due to unique constraint on user_id
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush(); // Force immediate execution of delete operation
        
        // Step 2: Create - Generate new refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        
        return token;
    }
    
    @Transactional
    public int deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return refreshTokenRepository.deleteByUser(user);
    }
}