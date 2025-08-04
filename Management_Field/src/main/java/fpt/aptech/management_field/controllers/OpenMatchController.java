package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.OpenMatch;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.OpenMatchDto;
import fpt.aptech.management_field.payload.request.CreateOpenMatchRequest;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.payload.response.ApiResponse;
import fpt.aptech.management_field.repositories.UserRepository;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import fpt.aptech.management_field.services.AIRecommendationService;
import fpt.aptech.management_field.services.OpenMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/open-matches")
public class OpenMatchController {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenMatchController.class);

    @Autowired
    private OpenMatchService openMatchService;
    
    @Autowired
    private AIRecommendationService aiRecommendationService;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final double MINIMUM_COMPATIBILITY_THRESHOLD = 0.6;

    @PostMapping
    public ResponseEntity<ApiResponse<OpenMatchDto>> createOpenMatch(@Valid @RequestBody CreateOpenMatchRequest request) {
        try {
            // Get current user ID if authenticated (optional)
            Long userId = null;
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && 
                    !"anonymousUser".equals(authentication.getPrincipal())) {
                    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                    userId = userDetails.getId();
                    logger.info("[CREATE_OPEN_MATCH] Authenticated user {} creating open match", userId);
                } else {
                    logger.info("[CREATE_OPEN_MATCH] Anonymous user creating open match");
                }
            } catch (Exception e) {
                logger.warn("[CREATE_OPEN_MATCH] Authentication check failed, proceeding as anonymous: {}", e.getMessage());
            }
            
            OpenMatchDto openMatch = openMatchService.createOpenMatch(request, userId);
            
            logger.info("[CREATE_OPEN_MATCH] Successfully created open match with ID {} for user {}", 
                       openMatch.getId(), userId != null ? userId : "anonymous");
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Open match created successfully", openMatch)
            );
        } catch (RuntimeException e) {
            logger.error("[CREATE_OPEN_MATCH] Error creating open match: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Error creating open match: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OpenMatchDto>>> getAllOpenMatches(
            @RequestParam(required = false) String sportType) {
        
        System.out.println("🚀🚀🚀 getAllOpenMatches CONTROLLER CALLED 🚀🚀🚀");
        System.out.println("🚀 sportType parameter: " + sportType);
        logger.info("[DEBUG] ===== getAllOpenMatches CONTROLLER CALLED =====");
        logger.info("[DEBUG] sportType parameter: {}", sportType);
        
        try {
            // Get current user ID if authenticated (optional)
            Long userId = null;
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                logger.info("[DEBUG] Authentication object: {}", authentication);
                if (authentication != null && authentication.isAuthenticated() && 
                    !"anonymousUser".equals(authentication.getPrincipal())) {
                    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                    userId = userDetails.getId();
                    logger.info("[DEBUG] Extracted userId: {}", userId);
                } else {
                    logger.info("[DEBUG] No authenticated user found or anonymous user");
                }
            } catch (Exception e) {
                logger.warn("[DEBUG] Error extracting userId: {}", e.getMessage());
                // If authentication fails, continue with null userId
            }
            
            List<OpenMatchDto> openMatches;
            if (sportType != null && !sportType.isEmpty()) {
                if (userId != null) {
                    openMatches = openMatchService.getOpenMatchesBySport(sportType, userId);
                } else {
                    openMatches = openMatchService.getOpenMatchesBySport(sportType);
                }
            } else {
                if (userId != null) {
                    openMatches = openMatchService.getAllOpenMatches(userId);
                } else {
                    openMatches = openMatchService.getAllOpenMatches();
                }
            }
            
            logger.info("[DEBUG] Successfully retrieved {} open matches", openMatches.size());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Open matches retrieved successfully", openMatches)
            );
            
        } catch (Exception e) {
            logger.error("[DEBUG] Error retrieving open matches: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve open matches: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/ranked")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OpenMatchDto>>> getRankedOpenMatches(
            @RequestParam(required = false) String sportType) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[AI_RANKING] getRankedOpenMatches called - userId: {}, sportType: {}", userDetails.getId(), sportType);
            
            List<OpenMatchDto> rankedMatches = openMatchService.getRankedOpenMatches(userDetails.getId(), sportType);
            
            logger.info("[AI_RANKING] Successfully retrieved {} ranked open matches for user {}", 
                       rankedMatches.size(), userDetails.getId());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Ranked open matches retrieved successfully", rankedMatches)
            );
            
        } catch (Exception e) {
            logger.error("[AI_RANKING] Error retrieving ranked open matches for user {}: {}", 
                        userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve ranked open matches: " + e.getMessage(), null));
        }
    }

    @GetMapping("/my-matches")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OpenMatchDto>>> getMyOpenMatches() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[MY_MATCHES] User {} requesting their open matches", userDetails.getId());
            
            List<OpenMatchDto> openMatches = openMatchService.getUserOpenMatches(userDetails.getId());
            
            logger.info("[MY_MATCHES] Successfully retrieved {} open matches for user {}", 
                       openMatches.size(), userDetails.getId());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "My open matches retrieved successfully", openMatches)
            );
            
        } catch (Exception e) {
            logger.error("[MY_MATCHES] Error retrieving open matches for user {}: {}", 
                        userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve my open matches: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> closeOpenMatch(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            openMatchService.closeOpenMatch(id, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Open match closed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error closing open match: " + e.getMessage()));
        }
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<OpenMatchDto> getOpenMatchByBooking(@PathVariable Long bookingId) {
        try {
            // Get current user ID if authenticated (optional)
            Long userId = null;
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && 
                    !"anonymousUser".equals(authentication.getPrincipal())) {
                    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                    userId = userDetails.getId();
                }
            } catch (Exception e) {
                // If authentication fails, continue with null userId
            }
            
            OpenMatchDto openMatch = openMatchService.getOpenMatchByBooking(bookingId, userId);
            return ResponseEntity.ok(openMatch);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> joinOpenMatch(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            openMatchService.joinOpenMatch(id, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Successfully joined the match"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error joining match: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}/join")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> leaveOpenMatch(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            openMatchService.leaveOpenMatch(id, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Successfully left the match"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error leaving match: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}/leave")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> leaveOpenMatchAlternative(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            openMatchService.leaveOpenMatch(id, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Successfully left the match"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error leaving match: " + e.getMessage()));
        }
    }
    
    @GetMapping("/ranked-v2")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<List<OpenMatchDto>> getRankedOpenMatchesV2(
            @RequestParam(required = false) String sportType) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        logger.info("[AI_RANKING] getRankedOpenMatchesV2 called - userId: {}, sportType: {}", userDetails.getId(), sportType);
        
        List<OpenMatchDto> rankedMatches = openMatchService.getRankedOpenMatchesV2(userDetails.getId(), sportType);
        return ResponseEntity.ok(rankedMatches);
    }

    // Recommend teammates for community open matches using same logic as RecommendationModal
    @GetMapping("/recommend-teammates")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> recommendTeammates(
            @RequestParam(required = false) String sportType,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            // Get the current user
            User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Get potential teammates (discoverable ROLE_USER only, excluding current user)
            List<User> potentialTeammates = userRepository.findDiscoverableRegularUsersExcludingUser(userId);
            
            // Use default sport type if not provided
            String finalSportType = sportType != null ? sportType : "BONG_DA";
            
            List<Map<String, Object>> recommendations;
            
            // Always try to use AI service for real recommendations
            try {
                // Try hybrid recommendation first, fallback to legacy if needed
                try {
                    recommendations = aiRecommendationService.recommendTeammatesHybrid(currentUser, potentialTeammates, finalSportType);
                } catch (Exception hybridException) {
                    recommendations = aiRecommendationService.recommendTeammates(currentUser, potentialTeammates, finalSportType);
                }
                
                // Validate that AI service returned proper scores
                boolean hasValidScores = recommendations.stream()
                    .anyMatch(rec -> rec.containsKey("compatibilityScore") && 
                             rec.get("compatibilityScore") instanceof Number &&
                             ((Number) rec.get("compatibilityScore")).doubleValue() != 0.5);
                
                if (!hasValidScores) {
                    throw new RuntimeException("AI service returned invalid or mock scores");
                }
                
                // Filter out low compatibility scores
                List<Map<String, Object>> filteredRecommendations = recommendations.stream()
                    .filter(rec -> {
                        if (rec.containsKey("compatibilityScore") && rec.get("compatibilityScore") instanceof Number) {
                            double score = ((Number) rec.get("compatibilityScore")).doubleValue();
                            return score >= MINIMUM_COMPATIBILITY_THRESHOLD;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
                
                recommendations = filteredRecommendations;
                
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "error", "AI recommendation service is currently unavailable",
                    "details", e.getMessage(),
                    "aiServiceAvailable", false
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "sportType", finalSportType,
                "recommendations", recommendations,
                "totalRecommendations", recommendations.size(),
                "aiServiceAvailable", aiRecommendationService.isAIServiceAvailable(),
                "message", "Teammate recommendations retrieved successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to get teammate recommendations: " + e.getMessage()
            ));
        }
    }
}