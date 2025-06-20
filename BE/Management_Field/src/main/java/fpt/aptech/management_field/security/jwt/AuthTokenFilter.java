package fpt.aptech.management_field.security.jwt;

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
            
            "/api/test",
            "/swagger-ui",
            "/v3/api-docs"
    );
    
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/booking/payment-callback",
            "/api/booking/payment-cancel",
            "/api/booking/success",
            "/api/booking/confirm",
            "/api/booking/paypal/capture" // Public PayPal capture endpoint
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        String method = request.getMethod();
        
        logger.info("=== shouldNotFilter check: {} {} ===", method, path);
        
        // Check if path starts with any public prefix
        boolean matchesPrefix = PUBLIC_PATH_PREFIXES.stream().anyMatch(prefix -> {
            boolean matches = path.startsWith(prefix) || path.startsWith(prefix + "/");
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
        
        // Allow GET requests to booking endpoints
        boolean isBookingGetRequest = "GET".equals(method) && path.startsWith("/api/booking/");
        if (isBookingGetRequest) {
            logger.info("Path {} is a booking GET request", path);
        }
        
        boolean shouldSkip = matchesPrefix || matchesExactPath || isBookingGetRequest;
        logger.info("=== shouldNotFilter FINAL result: {} (prefix: {}, exact: {}, booking GET: {}) ===", 
            shouldSkip, matchesPrefix, matchesExactPath, isBookingGetRequest);
        
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getServletPath();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");
        
        // Log incoming request details
        logger.info("=== AuthTokenFilter doFilterInternal: {} {} ===", method, requestPath);
        logger.info("Authorization header present: {}", authHeader != null ? "YES" : "NO");
        
        // Note: shouldNotFilter is automatically called by OncePerRequestFilter
        // If we reach this point, it means shouldNotFilter returned false
        logger.info("=== Processing JWT for authenticated endpoint: {} {} ===", method, requestPath);
        
        if (authHeader != null) {
            // Log header format but mask the actual token for security
            if (authHeader.startsWith("Bearer ")) {
                String tokenPreview = authHeader.length() > 30 ? 
                    authHeader.substring(0, 15) + "..." + authHeader.substring(authHeader.length() - 10) : 
                    "[SHORT_HEADER]";
                logger.info("Authorization header format: {}", tokenPreview);
            } else {
                logger.warn("Authorization header does not start with 'Bearer ': {}", 
                    authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader);
            }
        }

        // ĐÃ THAY ĐỔI: Xóa bỏ khối try-catch để cho phép các ngoại lệ xác thực được ném ra
        String jwt = parseJwt(request);
        
        if (jwt != null) {
            logger.info("JWT token extracted successfully from request");
            
            if (jwtUtils.validateJwtToken(jwt)) {
                logger.info("JWT token validation successful, proceeding with authentication");
                
                String username = jwtUtils.getUsernameFromJwtToken(jwt);
                logger.info("Username extracted from JWT: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("✅ USER AUTHENTICATED SUCCESSFULLY - Username: {}, Authorities: {}, Request: {} {}", 
                    username, userDetails.getAuthorities(), request.getMethod(), request.getRequestURI());
            } else {
                logger.error("❌ JWT VALIDATION FAILED in AuthTokenFilter:");
                logger.error("   Request: {} {}", request.getMethod(), request.getRequestURI());
                logger.error("   Token present: {}", jwt != null);
                if (jwt != null) {
                    logger.error("   Token preview: {}", jwt.substring(0, Math.min(jwt.length(), 30)) + "...");
                    logger.error("   Token length: {} characters", jwt.length());
                } else {
                    logger.error("   No JWT token found in Authorization header");
                }
                logger.error("   This will result in 401 Unauthorized response");
            }
        } else {
            logger.info("No JWT token found in request headers");
        }

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