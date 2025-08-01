package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.OpenMatch;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.OpenMatchDto;
import fpt.aptech.management_field.payload.request.CreateOpenMatchRequest;
import fpt.aptech.management_field.payload.response.MessageResponse;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/open-matches")
public class OpenMatchController {

    @Autowired
    private OpenMatchService openMatchService;
    
    @Autowired
    private AIRecommendationService aiRecommendationService;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final double MINIMUM_COMPATIBILITY_THRESHOLD = 0.6;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> createOpenMatch(@Valid @RequestBody CreateOpenMatchRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            OpenMatchDto openMatch = openMatchService.createOpenMatch(request, userDetails.getId());
            return ResponseEntity.ok(openMatch);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error creating open match: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<OpenMatchDto>> getAllOpenMatches(
            @RequestParam(required = false) String sportType) {
        
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
        
        return ResponseEntity.ok(openMatches);
    }
    
    @GetMapping("/ranked")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> getRankedOpenMatches(
            @RequestParam(required = false) String sportType) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            System.out.println("[DEBUG] getRankedOpenMatches called - userId: " + userDetails.getId() + ", sportType: " + sportType);
            
            List<OpenMatchDto> rankedMatches = openMatchService.getRankedOpenMatches(userDetails.getId(), sportType);
            
            System.out.println("[DEBUG] Successfully retrieved " + rankedMatches.size() + " ranked matches");
            
            return ResponseEntity.ok(rankedMatches);
            
        } catch (Exception e) {
            System.err.println("[ERROR] Exception in getRankedOpenMatches: " + e.getMessage());
            e.printStackTrace();
            
            // Return error response instead of letting it bubble up as 500
            return ResponseEntity.status(500).body(new MessageResponse("Error retrieving ranked matches: " + e.getMessage()));
        }
    }

    @GetMapping("/my-matches")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<List<OpenMatchDto>> getMyOpenMatches() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        List<OpenMatchDto> openMatches = openMatchService.getUserOpenMatches(userDetails.getId());
        return ResponseEntity.ok(openMatches);
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