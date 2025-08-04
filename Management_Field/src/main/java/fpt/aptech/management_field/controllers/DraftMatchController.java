package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.payload.dtos.DraftMatchDto;
import fpt.aptech.management_field.payload.dtos.UserDto; // Sửa từ UserDTO thành UserDto
import fpt.aptech.management_field.payload.request.CreateDraftMatchRequest;
import fpt.aptech.management_field.payload.request.ConvertDraftToMatchRequest;
import fpt.aptech.management_field.payload.request.UpdateDraftMatchRequest;
import fpt.aptech.management_field.payload.request.InitiateDraftMatchBookingRequest;
import fpt.aptech.management_field.payload.request.CompleteDraftMatchBookingRequest;
import fpt.aptech.management_field.payload.request.RecommendTeammatesRequest;
import fpt.aptech.management_field.payload.response.ApiResponse;
import fpt.aptech.management_field.services.DraftMatchService;
import fpt.aptech.management_field.services.UserService;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.repositories.UserRepository;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing Draft Matches - "Kèo Nháp" feature
 * Handles the complete workflow from creating draft matches to converting them to real matches
 */
@RestController
@RequestMapping("/api/draft-matches")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DraftMatchController {
    
    private static final Logger logger = LoggerFactory.getLogger(DraftMatchController.class);
    
    @Autowired
    private DraftMatchService draftMatchService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create a new Draft Match
     * POST /api/draft-matches
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DraftMatchDto>> createDraftMatch(
            @Valid @RequestBody CreateDraftMatchRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_CREATE] User {} creating draft match for sport: {}", 
                       userDetails.getId(), request.getSportType());
            
            User creator = userRepository.findById(userDetails.getId()).orElse(null);
            if (creator == null) {
                logger.error("[DRAFT_MATCH_CREATE] User {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            DraftMatchDto createdMatch = draftMatchService.createDraftMatch(request, userDetails.getId());
            
            logger.info("[DRAFT_MATCH_CREATE] Successfully created draft match {} for user {}", 
                       createdMatch.getId(), userDetails.getId());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Draft match created successfully", createdMatch)
            );
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_CREATE] Error creating draft match for user {}: {}", 
                        userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to create draft match: " + e.getMessage(), null));
        }
    }

    /**
     * Update a draft match (creator only)
     * PUT /api/draft-matches/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DraftMatchDto>> updateDraftMatch(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDraftMatchRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_UPDATE] User {} updating draft match {}", 
                       userDetails.getId(), id);
            
            User creator = userRepository.findById(userDetails.getId()).orElse(null);
            if (creator == null) {
                logger.error("[DRAFT_MATCH_UPDATE] Creator {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            DraftMatchDto result = draftMatchService.updateDraftMatch(id, request, userDetails.getId());
            
            if (result != null) {
                logger.info("[DRAFT_MATCH_UPDATE] Successfully updated draft match {} by user {}", 
                           id, userDetails.getId());
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "Draft match updated successfully", result)
                );
            } else {
                logger.warn("[DRAFT_MATCH_UPDATE] Failed to update draft match {} by user {}", 
                           id, userDetails.getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to update draft match", null));
            }
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_UPDATE] Error updating draft match {} by user {}: {}", 
                        id, userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to update draft match: " + e.getMessage(), null));
        }
    }

    /**
     * Get received draft match requests (for creator)
     * GET /api/draft-matches/received-requests
     */
    @GetMapping("/received-requests")
    public ResponseEntity<ApiResponse<List<Object>>> getReceivedDraftMatchRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_RECEIVED_REQUESTS] User {} requesting received draft match requests", 
                       userDetails.getId());
            
            User creator = userRepository.findById(userDetails.getId()).orElse(null);
            if (creator == null) {
                logger.error("[DRAFT_MATCH_RECEIVED_REQUESTS] User {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            List<Object> receivedRequests = draftMatchService.getReceivedDraftMatchRequests(userDetails.getId());
            
            logger.info("[DRAFT_MATCH_RECEIVED_REQUESTS] Successfully retrieved {} received requests for user {}", 
                       receivedRequests.size(), userDetails.getId());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Received requests retrieved successfully", receivedRequests)
            );
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_RECEIVED_REQUESTS] Error retrieving received requests for user {}: {}", 
                        userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve received requests: " + e.getMessage(), null));
        }
    }

    /**
     * Get sent draft match requests (for users)
     * GET /api/draft-matches/sent-requests
     */
    @GetMapping("/sent-requests")
    public ResponseEntity<ApiResponse<List<Object>>> getSentDraftMatchRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_SENT_REQUESTS] User {} requesting sent draft match requests", 
                       userDetails.getId());
            
            User user = userRepository.findById(userDetails.getId()).orElse(null);
            if (user == null) {
                logger.error("[DRAFT_MATCH_SENT_REQUESTS] User {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            List<Object> sentRequests = draftMatchService.getSentDraftMatchRequests(userDetails.getId());
            
            logger.info("[DRAFT_MATCH_SENT_REQUESTS] Successfully retrieved {} sent requests for user {}", 
                       sentRequests.size(), userDetails.getId());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Sent requests retrieved successfully", sentRequests)
            );
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_SENT_REQUESTS] Error retrieving sent requests for user {}: {}", 
                        userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve sent requests: " + e.getMessage(), null));
        }
    }

    /**
     * Remove an approved user from draft match (creator only)
     * DELETE /api/draft-matches/{id}/remove-user/{userId}
     */
    @DeleteMapping("/{id}/remove-user/{userId}")
    public ResponseEntity<ApiResponse<String>> removeApprovedUser(
            @PathVariable Long id,
            @PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_REMOVE_USER] User {} removing user {} from draft match {}", 
                       userDetails.getId(), userId, id);
            
            User creator = userRepository.findById(userDetails.getId()).orElse(null);
            if (creator == null) {
                logger.error("[DRAFT_MATCH_REMOVE_USER] Creator {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            DraftMatchDto result = draftMatchService.removeApprovedUser(id, userDetails.getId(), userId);
            
            if (result != null) {
                logger.info("[DRAFT_MATCH_REMOVE_USER] Successfully removed user {} from draft match {} by user {}", 
                           userId, id, userDetails.getId());
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "User removed successfully", "User has been removed from the draft match")
                );
            } else {
                logger.warn("[DRAFT_MATCH_REMOVE_USER] Failed to remove user {} from draft match {}", userId, id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to remove user", null));
            }
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_REMOVE_USER] Error removing user {} from draft match {}: {}", 
                        userId, id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to remove user: " + e.getMessage(), null));
        }
    }

    /**
     * Initiate draft match booking (creator only)
     * POST /api/draft-matches/{id}/initiate-booking
     */
    @PostMapping("/{id}/initiate-booking")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initiateDraftMatchBooking(
            @PathVariable Long id,
            @Valid @RequestBody InitiateDraftMatchBookingRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_INITIATE_BOOKING] User {} initiating booking for draft match {}", 
                       userDetails.getId(), id);
            
            User creator = userRepository.findById(userDetails.getId()).orElse(null);
            if (creator == null) {
                logger.error("[DRAFT_MATCH_INITIATE_BOOKING] Creator {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            Map<String, Object> result = draftMatchService.initiateDraftMatchBooking(id, userDetails.getId(), request);
            
            logger.info("[DRAFT_MATCH_INITIATE_BOOKING] Successfully initiated booking for draft match {} by user {}", 
                       id, userDetails.getId());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Draft match booking initiated successfully", result)
            );
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_INITIATE_BOOKING] Error initiating booking for draft match {} by user {}: {}", 
                        id, userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to initiate booking: " + e.getMessage(), null));
        }
    }

    /**
     * Complete draft match booking (creator only)
     * POST /api/draft-matches/{id}/complete-booking
     */
    @PostMapping("/{id}/complete-booking")
    public ResponseEntity<ApiResponse<Map<String, Object>>> completeDraftMatchBooking(
            @PathVariable Long id,
            @Valid @RequestBody CompleteDraftMatchBookingRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_COMPLETE_BOOKING] User {} completing booking for draft match {}", 
                       userDetails.getId(), id);
            
            User creator = userRepository.findById(userDetails.getId()).orElse(null);
            if (creator == null) {
                logger.error("[DRAFT_MATCH_COMPLETE_BOOKING] Creator {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            Map<String, Object> result = draftMatchService.completeDraftMatchBooking(id, userDetails.getId(), request);
            
            logger.info("[DRAFT_MATCH_COMPLETE_BOOKING] Successfully completed booking for draft match {} by user {}", 
                       id, userDetails.getId());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Draft match booking completed successfully", result)
            );
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_COMPLETE_BOOKING] Error completing booking for draft match {} by user {}: {}", 
                        id, userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to complete booking: " + e.getMessage(), null));
        }
    }

    /**
     * Recommend teammates for draft match
     * POST /api/draft-matches/recommend-teammates
     */
    @PostMapping("/recommend-teammates")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> recommendTeammates(
            @Valid @RequestBody RecommendTeammatesRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_RECOMMEND_TEAMMATES] User {} requesting teammate recommendations", 
                       userDetails.getId());
            
            User user = userRepository.findById(userDetails.getId()).orElse(null);
            if (user == null) {
                logger.error("[DRAFT_MATCH_RECOMMEND_TEAMMATES] User {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            List<Map<String, Object>> recommendations = draftMatchService.recommendTeammates(userDetails.getId(), request);
            
            logger.info("[DRAFT_MATCH_RECOMMEND_TEAMMATES] Successfully retrieved {} teammate recommendations for user {}", 
                       recommendations.size(), userDetails.getId());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Teammate recommendations retrieved successfully", recommendations)
            );
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_RECOMMEND_TEAMMATES] Error retrieving teammate recommendations for user {}: {}", 
                        userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve teammate recommendations: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get all active draft matches
     * GET /api/draft-matches
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DraftMatchDto>>> getActiveDraftMatches(
            @RequestParam(required = false) String sportType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_ACTIVE] User {} requesting active draft matches for sport: {}", 
                       userDetails.getId(), sportType);
            
            User currentUser = userRepository.findById(userDetails.getId()).orElse(null);
            if (currentUser == null) {
                logger.error("[DRAFT_MATCH_ACTIVE] User {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            List<DraftMatchDto> activeMatches = draftMatchService.getAllActiveDraftMatches(userDetails.getId());
            
            logger.info("[DRAFT_MATCH_ACTIVE] Successfully retrieved {} active draft matches for user {}", 
                       activeMatches.size(), userDetails.getId());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Active draft matches retrieved successfully", activeMatches)
            );
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_ACTIVE] Error retrieving active draft matches for user {}: {}", 
                        userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve active draft matches: " + e.getMessage(), null));
        }
    }

    /**
     * Get public draft matches (no authentication required)
     * GET /api/draft-matches/public
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<DraftMatchDto>>> getPublicDraftMatches(
            @RequestParam(required = false) String sportType) {
        try {
            logger.info("[DRAFT_MATCH_PUBLIC] Requesting public draft matches for sport: {}", sportType);
            
            List<DraftMatchDto> publicMatches = draftMatchService.getPublicDraftMatches(sportType);
            
            logger.info("[DRAFT_MATCH_PUBLIC] Successfully retrieved {} public draft matches", 
                       publicMatches.size());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Public draft matches retrieved successfully", publicMatches)
            );
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_PUBLIC] Error retrieving public draft matches: {}", 
                        e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve public draft matches: " + e.getMessage(), null));
        }
    }

    /**
     * Get ranked draft matches using AI
     * GET /api/draft-matches/ranked
     */
    @GetMapping("/ranked")
    public ResponseEntity<ApiResponse<List<DraftMatchDto>>> getRankedDraftMatches(
            @RequestParam(required = false) String sport) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_RANKED] User {} requesting ranked draft matches for sport: {}", 
                       userDetails.getId(), sport);
            
            User currentUser = userRepository.findById(userDetails.getId()).orElse(null);
            if (currentUser == null) {
                logger.error("[DRAFT_MATCH_RANKED] User {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            List<DraftMatchDto> rankedMatches = draftMatchService.getRankedDraftMatches(userDetails.getId(), sport);
            
            logger.info("[DRAFT_MATCH_RANKED] Successfully retrieved {} ranked draft matches for user {}", 
                       rankedMatches.size(), userDetails.getId());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Ranked draft matches retrieved successfully", rankedMatches)
            );
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_RANKED] Error retrieving ranked draft matches for user {}: {}", 
                        userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve ranked draft matches: " + e.getMessage(), null));
        }
    }
    
    /**
     * Express interest in a draft match
     * POST /api/draft-matches/{id}/express-interest
     */
    @PostMapping("/{id}/express-interest")
    public ResponseEntity<ApiResponse<String>> expressInterest(
            @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_INTEREST] User {} expressing interest in draft match {}", 
                       userDetails.getId(), id);
            
            DraftMatchDto result = draftMatchService.expressInterest(id, userDetails.getId());
            
            if (result != null) {
                logger.info("[DRAFT_MATCH_INTEREST] User {} successfully expressed interest in draft match {}", 
                           userDetails.getId(), id);
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "Interest expressed successfully", "Interest recorded")
                );
            } else {
                logger.warn("[DRAFT_MATCH_INTEREST] User {} failed to express interest in draft match {}", 
                           userDetails.getId(), id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to express interest", null));
            }
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_INTEREST] Error expressing interest for user {} in draft match {}: {}", 
                        userDetails.getId(), id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to express interest: " + e.getMessage(), null));
        }
    }
    
    /**
     * Withdraw interest from a draft match
     * DELETE /api/draft-matches/{id}/withdraw-interest
     */
    @DeleteMapping("/{id}/withdraw-interest")
    public ResponseEntity<ApiResponse<String>> withdrawInterest(
            @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_WITHDRAW] User {} withdrawing interest from draft match {}", 
                       userDetails.getId(), id);
            
            DraftMatchDto result = draftMatchService.withdrawInterest(id, userDetails.getId());
            
            if (result != null) {
                logger.info("[DRAFT_MATCH_WITHDRAW] User {} successfully withdrew interest from draft match {}", 
                           userDetails.getId(), id);
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "Interest withdrawn successfully", "Interest removed")
                );
            } else {
                logger.warn("[DRAFT_MATCH_WITHDRAW] User {} failed to withdraw interest from draft match {}", 
                           userDetails.getId(), id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to withdraw interest", null));
            }
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_WITHDRAW] Error withdrawing interest for user {} from draft match {}: {}", 
                        userDetails.getId(), id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to withdraw interest: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get list of users interested in a draft match (for creator only)
     * GET /api/draft-matches/{id}/interested-users
     */
    @GetMapping("/{id}/interested-users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getInterestedUsers(
            @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_INTERESTED] User {} requesting interested users for draft match {}", 
                       userDetails.getId(), id);
            
            User creator = userRepository.findById(userDetails.getId()).orElse(null);
            if (creator == null) {
                logger.error("[DRAFT_MATCH_INTERESTED] User {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            List<Map<String, Object>> interestedUsers = draftMatchService.getInterestedUsersWithCompatibility(id, userDetails.getId());
            
            logger.info("[DRAFT_MATCH_INTERESTED] Successfully retrieved {} interested users for draft match {}", 
                       interestedUsers.size(), id);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Interested users retrieved successfully", interestedUsers)
            );
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_INTERESTED] Error retrieving interested users for draft match {}: {}", 
                        id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve interested users: " + e.getMessage(), null));
        }
    }
    
    /**
     * Accept a user for the draft match (creator only)
     * POST /api/draft-matches/{id}/accept-user/{userId}
     */
    @PostMapping("/{id}/accept-user/{userId}")
    public ResponseEntity<ApiResponse<String>> acceptUser(
            @PathVariable Long id,
            @PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_ACCEPT] User {} accepting user {} for draft match {}", 
                       userDetails.getId(), userId, id);
            
            User creator = userRepository.findById(userDetails.getId()).orElse(null);
            if (creator == null) {
                logger.error("[DRAFT_MATCH_ACCEPT] Creator {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            User userToAccept = userRepository.findById(userId).orElse(null);
            if (userToAccept == null) {
                logger.error("[DRAFT_MATCH_ACCEPT] User to accept {} not found", userId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "User to accept not found", null));
            }
            
            DraftMatchDto result = draftMatchService.acceptUser(id, userDetails.getId(), userId);
            
            if (result != null) {
                logger.info("[DRAFT_MATCH_ACCEPT] User {} successfully accepted user {} for draft match {}", 
                           userDetails.getId(), userId, id);
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "User accepted successfully", "User has been accepted")
                );
            } else {
                logger.warn("[DRAFT_MATCH_ACCEPT] Failed to accept user {} for draft match {}", userId, id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to accept user", null));
            }
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_ACCEPT] Error accepting user {} for draft match {}: {}", 
                        userId, id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to accept user: " + e.getMessage(), null));
        }
    }
    
    /**
     * Reject a user for the draft match (creator only)
     * POST /api/draft-matches/{id}/reject-user/{userId}
     */
    @PostMapping("/{id}/reject-user/{userId}")
    public ResponseEntity<ApiResponse<String>> rejectUser(
            @PathVariable Long id,
            @PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_REJECT] User {} rejecting user {} for draft match {}", 
                       userDetails.getId(), userId, id);
            
            User creator = userRepository.findById(userDetails.getId()).orElse(null);
            if (creator == null) {
                logger.error("[DRAFT_MATCH_REJECT] Creator {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            User userToReject = userRepository.findById(userId).orElse(null);
            if (userToReject == null) {
                logger.error("[DRAFT_MATCH_REJECT] User to reject {} not found", userId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "User to reject not found", null));
            }
            
            DraftMatchDto result = draftMatchService.rejectUser(id, userDetails.getId(), userId);
            
            if (result != null) {
                logger.info("[DRAFT_MATCH_REJECT] User {} successfully rejected user {} for draft match {}", 
                           userDetails.getId(), userId, id);
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "User rejected successfully", "User has been rejected")
                );
            } else {
                logger.warn("[DRAFT_MATCH_REJECT] Failed to reject user {} for draft match {}", userId, id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to reject user", null));
            }
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_REJECT] Error rejecting user {} for draft match {}: {}", 
                        userId, id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to reject user: " + e.getMessage(), null));
        }
    }
    
    /**
     * Convert draft match to real match and book field
     * POST /api/draft-matches/{id}/convert-to-match
     */
    @PostMapping("/{id}/convert-to-match")
    public ResponseEntity<ApiResponse<Map<String, Object>>> convertToMatch(
            @PathVariable Long id,
            @Valid @RequestBody ConvertDraftToMatchRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_CONVERT] User {} converting draft match {} to real match", 
                       userDetails.getId(), id);
            
            User creator = userRepository.findById(userDetails.getId()).orElse(null);
            if (creator == null) {
                logger.error("[DRAFT_MATCH_CONVERT] Creator {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            Object result = draftMatchService.convertToMatch(id, userDetails.getId());
            Map<String, Object> resultMap = (Map<String, Object>) result;
            
            logger.info("[DRAFT_MATCH_CONVERT] Successfully converted draft match {} to real match", id);
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Draft match converted successfully", resultMap)
            );
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_CONVERT] Error converting draft match {} to real match: {}", 
                        id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to convert draft match: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get draft match details by ID
     * GET /api/draft-matches/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DraftMatchDto>> getDraftMatchById(
            @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_DETAIL] User {} requesting draft match details for {}", 
                       userDetails.getId(), id);
            
            User currentUser = userRepository.findById(userDetails.getId()).orElse(null);
            if (currentUser == null) {
                logger.error("[DRAFT_MATCH_DETAIL] User {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            DraftMatchDto draftMatch = draftMatchService.getDraftMatchById(id, userDetails.getId());
            
            if (draftMatch != null) {
                logger.info("[DRAFT_MATCH_DETAIL] Successfully retrieved draft match {} for user {}", 
                           id, userDetails.getId());
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "Draft match retrieved successfully", draftMatch)
                );
            } else {
                logger.warn("[DRAFT_MATCH_DETAIL] Draft match {} not found or not accessible for user {}", 
                           id, userDetails.getId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Draft match not found", null));
            }
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_DETAIL] Error retrieving draft match {} for user {}: {}", 
                        id, userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve draft match: " + e.getMessage(), null));
        }
    }
    
    /**
     * Cancel a draft match (creator only)
     * DELETE /api/draft-matches/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> cancelDraftMatch(
            @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_CANCEL] User {} canceling draft match {}", 
                       userDetails.getId(), id);
            
            User creator = userRepository.findById(userDetails.getId()).orElse(null);
            if (creator == null) {
                logger.error("[DRAFT_MATCH_CANCEL] Creator {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            boolean success = draftMatchService.cancelDraftMatch(id, userDetails.getId());
            
            if (success) {
                logger.info("[DRAFT_MATCH_CANCEL] Successfully canceled draft match {} by user {}", 
                           id, userDetails.getId());
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "Draft match canceled successfully", "Draft match has been canceled")
                );
            } else {
                logger.warn("[DRAFT_MATCH_CANCEL] Failed to cancel draft match {} by user {}", 
                           id, userDetails.getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to cancel draft match", null));
            }
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_CANCEL] Error canceling draft match {} by user {}: {}", 
                        id, userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to cancel draft match: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get user's own draft matches (created by the user)
     * GET /api/draft-matches/my-drafts
     */
    @GetMapping("/my-drafts")
    public ResponseEntity<ApiResponse<List<DraftMatchDto>>> getMyDraftMatches() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_MY_DRAFTS] User {} requesting own draft matches", 
                       userDetails.getId());
            
            User creator = userRepository.findById(userDetails.getId()).orElse(null);
            if (creator == null) {
                logger.error("[DRAFT_MATCH_MY_DRAFTS] User {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            List<DraftMatchDto> myDrafts = draftMatchService.getMyDraftMatches(userDetails.getId());
            
            logger.info("[DRAFT_MATCH_MY_DRAFTS] Successfully retrieved {} draft matches for user {}", 
                       myDrafts.size(), userDetails.getId());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "My draft matches retrieved successfully", myDrafts)
            );
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_MY_DRAFTS] Error retrieving draft matches for user {}: {}", 
                        userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve my draft matches: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get draft matches where user has expressed interest
     * GET /api/draft-matches/my-interests
     */
    @GetMapping("/my-interests")
    public ResponseEntity<ApiResponse<List<DraftMatchDto>>> getMyInterests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            logger.info("[DRAFT_MATCH_MY_INTERESTS] User {} requesting draft matches with expressed interest", 
                       userDetails.getId());
            
            User user = userRepository.findById(userDetails.getId()).orElse(null);
            if (user == null) {
                logger.error("[DRAFT_MATCH_MY_INTERESTS] User {} not found", userDetails.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not found", null));
            }
            
            List<DraftMatchDto> myInterests = draftMatchService.getMyInterests(userDetails.getId());
            
            logger.info("[DRAFT_MATCH_MY_INTERESTS] Successfully retrieved {} draft matches with interest for user {}", 
                       myInterests.size(), userDetails.getId());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "My interests retrieved successfully", myInterests)
            );
            
        } catch (Exception e) {
            logger.error("[DRAFT_MATCH_MY_INTERESTS] Error retrieving interests for user {}: {}", 
                        userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve my interests: " + e.getMessage(), null));
        }
    }
}