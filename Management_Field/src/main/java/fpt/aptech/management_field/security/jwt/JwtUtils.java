package fpt.aptech.management_field.security.jwt;

import fpt.aptech.management_field.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs:86400000}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            logger.debug("JWT secret key decoded successfully. Key length: {} bytes", keyBytes.length);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            logger.error("Failed to decode JWT secret key - Check if jwt.secret is properly Base64 encoded: {}", e.getMessage());
            throw e;
        }
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            // Log token validation attempt with first/last 10 characters for debugging
            String tokenPreview = authToken.length() > 20 ? 
                authToken.substring(0, 10) + "..." + authToken.substring(authToken.length() - 10) : 
                "[SHORT_TOKEN]";
            logger.info("🔍 VALIDATING JWT TOKEN: {}", tokenPreview);
            
            Claims claims = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken).getBody();
            
            // Log successful validation with token details
            logger.info("✅ JWT TOKEN VALIDATION SUCCESSFUL - Subject: {}, Issued: {}, Expires: {}", 
                claims.getSubject(), claims.getIssuedAt(), claims.getExpiration());
            
            return true;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.error("❌ JWT SIGNATURE VALIDATION FAILED - Invalid signature detected. This usually means:");
            logger.error("   1. Token was signed with a different secret key");
            logger.error("   2. Token has been tampered with");
            logger.error("   3. JWT secret configuration mismatch between services");
            logger.error("   Error details: {}", e.getMessage());
            logger.error("   Token preview: {}", authToken.length() > 30 ? authToken.substring(0, 30) + "..." : authToken);
        } catch (MalformedJwtException e) {
            logger.error("❌ JWT TOKEN MALFORMED - Invalid token format detected. This usually means:");
            logger.error("   1. Token structure is corrupted (missing parts)");
            logger.error("   2. Token encoding is invalid");
            logger.error("   3. Token was not properly generated");
            logger.error("   Error details: {}", e.getMessage());
            logger.error("   Token preview: {}", authToken.length() > 30 ? authToken.substring(0, 30) + "..." : authToken);
        } catch (ExpiredJwtException e) {
            logger.error("❌ JWT TOKEN EXPIRED - Token has passed its expiration time:");
            logger.error("   Token expired at: {}", e.getClaims().getExpiration());
            logger.error("   Current server time: {}", new Date());
            logger.error("   Time difference: {} ms", new Date().getTime() - e.getClaims().getExpiration().getTime());
            logger.error("   Subject: {}", e.getClaims().getSubject());
            logger.error("   This indicates the frontend needs to refresh the token");
        } catch (UnsupportedJwtException e) {
            logger.error("❌ JWT TOKEN UNSUPPORTED - Token type not supported:");
            logger.error("   Error details: {}", e.getMessage());
            logger.error("   This usually means the JWT algorithm or format is not supported");
        } catch (IllegalArgumentException e) {
            logger.error("❌ JWT TOKEN EMPTY OR NULL - Invalid token provided:");
            logger.error("   Error details: {}", e.getMessage());
            logger.error("   Token value: '{}'", authToken);
            logger.error("   This usually means no token was sent or token is empty");
        } catch (Exception e) {
            logger.error("❌ UNEXPECTED JWT VALIDATION ERROR - Unhandled exception:");
            logger.error("   Exception class: {}", e.getClass().getSimpleName());
            logger.error("   Error message: {}", e.getMessage());
            logger.error("   Stack trace:", e);
        }

        return false;
    }
}