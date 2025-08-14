package fpt.aptech.management_field.security.jwt;

import fpt.aptech.management_field.security.jwt.JwtUtils;
import fpt.aptech.management_field.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    private static final List<String> PUBLIC_PATH_PREFIXES = Arrays.asList(
            "/api/auth",
            "/api/fields",
            "/api/locations",
            "/api/location-reviews",
            "/api/chatbot",
            "/api/test",
            "/api/debug", // Debug endpoints
            "/api/booking/receipt", // Public booking receipt endpoints
            "/swagger-ui",
            "/v3/api-docs"
    );
    
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/open-matches", // Only the base endpoint is public
            "/api/booking/payment-callback",
            "/api/booking/payment-cancel",
            "/api/booking/success",
            "/api/booking/confirm",
            "/api/booking/paypal/capture", // Public PayPal capture endpoint
            "/api/payment/callback", // VNPay callback endpoint
            "/api/payment/success",
            "/api/payment/cancel"
    );
    
    // Paths that are public only for GET requests
    private static final List<String> PUBLIC_GET_PATHS = Arrays.asList(
            "/api/draft-matches" // Allow public GET access to draft matches
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        String method = request.getMethod();
        
        logger.info("=== shouldNotFilter check: {} {} ===", method, path);
        
        // Check if path starts with any public prefix
        boolean matchesPrefix = PUBLIC_PATH_PREFIXES.stream().anyMatch(prefix -> {
            boolean matches = path.startsWith(prefix);
            if (matches) {
                logger.info("Path {} matches prefix: {}", path, prefix);
            }
            return matches;
        });
        
        // Check if path exactly matches any public path
        boolean matchesExactPath = PUBLIC_PATHS.contains(path);
        if (matchesExactPath) {
            logger.info("Path {} matches exact public path", path);
        }
        
        // Check if path matches public GET paths and method is GET
        boolean matchesPublicGetPath = "GET".equals(method) && PUBLIC_GET_PATHS.contains(path);
        if (matchesPublicGetPath) {
            logger.info("Path {} matches public GET path for GET method", path);
        }
        
        boolean shouldSkip = matchesPrefix || matchesExactPath || matchesPublicGetPath;
        logger.info("=== shouldNotFilter FINAL result: {} (prefix: {}, exact: {}, publicGet: {}) ===", 
            shouldSkip, matchesPrefix, matchesExactPath, matchesPublicGetPath);
        
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        String method = request.getMethod();
        logger.info("=== doFilterInternal called for: {} {} ===", method, path);
        
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUsernameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null,
                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("=== Authentication set for user: {} ===", username);
            } else {
                logger.info("=== No valid JWT token found for: {} {} ===", method, path);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        logger.info("=== Proceeding with filter chain for: {} {} ===", method, path);
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        
        if (StringUtils.hasText(headerAuth)) {
            if (headerAuth.startsWith("Bearer ")) {
                String token = headerAuth.substring(7);
                logger.debug("JWT token parsed from Authorization header. Token length: {}", token.length());
                return token;
            } else {
                logger.warn("Authorization header present but does not start with 'Bearer ': {}", 
                    headerAuth.length() > 20 ? headerAuth.substring(0, 20) + "..." : headerAuth);
            }
        } else {
            logger.debug("No Authorization header found in request");
        }
        
        return null;
    }
}