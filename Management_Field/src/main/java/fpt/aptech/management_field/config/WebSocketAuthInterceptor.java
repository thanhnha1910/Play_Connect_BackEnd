package fpt.aptech.management_field.config;

import fpt.aptech.management_field.security.jwt.JwtUtils;
import fpt.aptech.management_field.security.services.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            logger.info("🔌 WebSocket CONNECT attempt detected - Starting JWT authentication");
            
            try {
                // Extract Authorization header from STOMP headers
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                logger.info("📋 Authorization header: {}", authHeader != null ? "Present" : "Missing");
                
                if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                    String jwt = authHeader.substring(7);
                    logger.info("🎫 JWT token extracted from Authorization header (length: {})", jwt.length());
                    
                    // Validate the JWT token
                    if (jwtUtils.validateJwtToken(jwt)) {
                        String username = jwtUtils.getUsernameFromJwtToken(jwt);
                        logger.info("✅ JWT validation successful for user: {}", username);
                        
                        // Load user details
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        // Create authentication object
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                        
                        // Set the authentication in the accessor
                        accessor.setUser(authentication);
                        
                        logger.info("🔐 WebSocket authentication successful for user: {} with roles: {}", 
                            username, userDetails.getAuthorities());
                        
                    } else {
                        logger.error("❌ JWT token validation failed - Rejecting WebSocket connection");
                        throw new SecurityException("Invalid JWT token");
                    }
                } else {
                    logger.error("❌ No valid Authorization header found - Expected 'Bearer <token>' format");
                    throw new SecurityException("Missing or invalid Authorization header");
                }
                
            } catch (Exception e) {
                logger.error("❌ WebSocket authentication failed: {}", e.getMessage());
                throw new SecurityException("WebSocket authentication failed: " + e.getMessage());
            }
        }
        
        return message;
    }
}