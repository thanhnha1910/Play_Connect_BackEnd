package fpt.aptech.management_field.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.aptech.management_field.models.AuthProvider;
import fpt.aptech.management_field.models.ERole;
import fpt.aptech.management_field.models.RefreshToken;
import fpt.aptech.management_field.models.Role;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.security.services.RefreshTokenService;
import fpt.aptech.management_field.repositories.RoleRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import fpt.aptech.management_field.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OAuth2Service {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String facebookClientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String facebookClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String facebookRedirectUri;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OAuth2LoginResult processOAuth2Login(String code, String provider) {
        try {
            // Step 1: Exchange code for access token
            String accessToken = exchangeCodeForToken(code, provider);
            
            // Step 2: Get user info using access token
            JsonNode userInfo = getUserInfo(accessToken, provider);
            
            // Step 3: Process user in database
            User user = processOAuthPostLogin(userInfo, provider);
            
            // Step 4: Generate JWT and refresh token
            String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
            
            return new OAuth2LoginResult(jwt, refreshToken.getToken(), user);
            
        } catch (Exception e) {
            throw new RuntimeException("OAuth2 authentication failed: " + e.getMessage(), e);
        }
    }
    
    // Inner class to hold OAuth2 login result
    public static class OAuth2LoginResult {
        private final String accessToken;
        private final String refreshToken;
        private final User user;
        
        public OAuth2LoginResult(String accessToken, String refreshToken, User user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }
        
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public User getUser() { return user; }
    }

    private String exchangeCodeForToken(String code, String provider) {
        String tokenUrl;
        String clientId;
        String clientSecret;
        String redirectUri;

        switch (provider.toLowerCase()) {
            case "google":
                tokenUrl = "https://oauth2.googleapis.com/token";
                clientId = googleClientId;
                clientSecret = googleClientSecret;
                redirectUri = googleRedirectUri;
                break;
            case "facebook":
                tokenUrl = "https://graph.facebook.com/v18.0/oauth/access_token";
                clientId = facebookClientId;
                clientSecret = facebookClientSecret;
                redirectUri = facebookRedirectUri;
                break;
            default:
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
        }

        logger.info("Exchanging code for token with provider: {}", provider);
        logger.debug("Token URL: {}", tokenUrl);
        logger.debug("Client ID: {}", clientId);
        logger.debug("Redirect URI: {}", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        try {
            logger.info("Making token exchange request to: {}", tokenUrl);
            logger.info("Request parameters: code={}, client_id={}, redirect_uri={}, grant_type=authorization_code", 
                       code.substring(0, Math.min(10, code.length())) + "...", clientId, redirectUri);
            
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            logger.info("Token exchange response status: {}", response.getStatusCode());
            logger.debug("Token exchange response body: {}", response.getBody());
            
            JsonNode responseBody = objectMapper.readTree(response.getBody());
            
            if (responseBody.has("error")) {
                String error = responseBody.get("error").asText();
                String errorDescription = responseBody.has("error_description") ? 
                    responseBody.get("error_description").asText() : "No description";
                logger.error("OAuth2 token exchange error: {} - {}", error, errorDescription);
                throw new RuntimeException("OAuth2 token exchange failed: " + error + " - " + errorDescription);
            }
            
            if (!responseBody.has("access_token")) {
                logger.error("No access_token in response: {}", response.getBody());
                throw new RuntimeException("No access_token received from OAuth2 provider");
            }
            
            return responseBody.get("access_token").asText();
        } catch (Exception e) {
            logger.error("Failed to exchange code for token with provider {}: {}", provider, e.getMessage(), e);
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                org.springframework.web.client.HttpClientErrorException httpError = (org.springframework.web.client.HttpClientErrorException) e;
                logger.error("HTTP Error Status: {}", httpError.getStatusCode());
                logger.error("HTTP Error Response Body: {}", httpError.getResponseBodyAsString());
            }
            throw new RuntimeException("Failed to exchange code for token: " + e.getMessage(), e);
        }
    }

    private JsonNode getUserInfo(String accessToken, String provider) {
        String userInfoUrl;
        
        switch (provider.toLowerCase()) {
            case "google":
                userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
                break;
            case "facebook":
                // Explicitly request email field from Facebook
                userInfoUrl = "https://graph.facebook.com/me?fields=id,name,email,picture.type(large)";
                break;
            default:
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
        }

        logger.info("Getting user info from provider: {}", provider);
        logger.debug("User info URL: {}", userInfoUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                userInfoUrl, HttpMethod.GET, entity, String.class);
            
            logger.debug("User info response status: {}", response.getStatusCode());
            logger.debug("User info response body: {}", response.getBody());
            
            JsonNode userInfo = objectMapper.readTree(response.getBody());
            
            // Check for Facebook API errors
            if (userInfo.has("error")) {
                JsonNode error = userInfo.get("error");
                String errorMessage = error.has("message") ? error.get("message").asText() : "Unknown error";
                String errorType = error.has("type") ? error.get("type").asText() : "Unknown type";
                logger.error("Facebook API error: {} - {}", errorType, errorMessage);
                throw new RuntimeException("Facebook API error: " + errorType + " - " + errorMessage);
            }
            
            return userInfo;
        } catch (Exception e) {
            logger.error("Failed to get user info from provider {}: {}", provider, e.getMessage(), e);
            throw new RuntimeException("Failed to get user info: " + e.getMessage(), e);
        }
    }

    private User processOAuthPostLogin(JsonNode userInfo, String provider) {
        String email;
        String name;
        String imageUrl;
        String providerId;

        switch (provider.toLowerCase()) {
            case "google":
                email = userInfo.get("email").asText();
                name = userInfo.get("name").asText();
                imageUrl = userInfo.get("picture").asText();
                providerId = userInfo.get("id").asText();
                break;
            case "facebook":
                logger.debug("Processing Facebook user info: {}", userInfo.toString());
                
                // Check if email is provided by Facebook
                if (!userInfo.has("email") || userInfo.get("email").isNull() || userInfo.get("email").asText().isEmpty()) {
                    logger.error("Facebook did not provide email. User info: {}", userInfo.toString());
                    throw new RuntimeException("Could not obtain email from Facebook. Please ensure your Facebook account has a verified email address and that you have granted email permission to this application.");
                }
                
                email = userInfo.get("email").asText();
                name = userInfo.get("name").asText();
                
                // Handle Facebook picture URL structure
                if (userInfo.has("picture") && userInfo.get("picture").has("data") && 
                    userInfo.get("picture").get("data").has("url")) {
                    imageUrl = userInfo.get("picture").get("data").get("url").asText();
                } else {
                    imageUrl = null;
                    logger.debug("No picture URL found in Facebook response");
                }
                
                providerId = userInfo.get("id").asText();
                logger.info("Successfully processed Facebook user: email={}, name={}", email, name);
                break;
            default:
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
        }

        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Email not provided by OAuth2 provider");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Update user info if needed
            if (imageUrl != null && !imageUrl.equals(user.getImageUrl())) {
                user.setImageUrl(imageUrl);
            }
            if (name != null && !name.equals(user.getFullName())) {
                user.setFullName(name);
            }
            // Update provider info if it was a local account
            if (user.getProvider() == null || user.getProvider() == AuthProvider.LOCAL) {
                user.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
                user.setProviderId(providerId);
            }
            
            // CRITICAL FIX: Ensure existing user has at least ROLE_USER
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                Set<Role> roles = new HashSet<>();
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
                user.setRoles(roles);
                logger.info("Assigned default ROLE_USER to existing user: {}", user.getEmail());
            }
        } else {
            // Create new user
            user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setUsername(email); // Use email as username for OAuth2 users
            user.setImageUrl(imageUrl);
            user.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
            user.setProviderId(providerId);
            user.setEmailVerified(true); // OAuth2 emails are considered verified
            user.setActive(true);
            // No password for OAuth2 users
            user.setPassword(null);
            // Khởi tạo memberLevel và bookingCount
            user.setMemberLevel(0);
            user.setBookingCount(0);

            // Assign default role
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }
}