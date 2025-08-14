package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.services.TimeConflictValidationService;
import fpt.aptech.management_field.services.TimeConflictValidationService.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling time conflict validation requests
 */
@RestController
@RequestMapping("/api/time-conflict")
@RequiredArgsConstructor
@Slf4j
public class TimeConflictController {

    private final TimeConflictValidationService timeConflictValidationService;

    /**
     * Validate if a user can join a draft match without time conflicts
     */
    @PostMapping("/validate-draft-match")
    public ResponseEntity<ValidationResult> validateDraftMatchJoin(
            @RequestParam Long userId,
            @RequestParam Long draftMatchId) {
        
        log.info("Validating draft match join for user {} and match {}", userId, draftMatchId);
        
        ValidationResult result = timeConflictValidationService.validateDraftMatchJoin(draftMatchId, userId);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Validate if a user can join an open match without time conflicts
     */
    @PostMapping("/validate-open-match")
    public ResponseEntity<ValidationResult> validateOpenMatchJoin(
            @RequestParam Long userId,
            @RequestParam Long openMatchId) {
        
        log.info("Validating open match join for user {} and match {}", userId, openMatchId);
        
        ValidationResult result = timeConflictValidationService.validateOpenMatchJoin(openMatchId, userId);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Get detailed conflict information for debugging
     */
    @GetMapping("/conflicts/{userId}")
    public ResponseEntity<ValidationResult> getUserConflicts(
            @PathVariable Long userId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) String location) {
        
        log.info("Getting conflicts for user {} from {} to {} at {}", userId, startTime, endTime, location);
        
        try {
            java.time.LocalDateTime start = java.time.LocalDateTime.parse(startTime);
            java.time.LocalDateTime end = java.time.LocalDateTime.parse(endTime);
            
            ValidationResult result = timeConflictValidationService.checkTimeConflictsForUser(
                userId, start, end, location != null ? location : "");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error parsing time parameters", e);
            return ResponseEntity.badRequest().body(
                ValidationResult.error("Invalid time format. Use ISO format: yyyy-MM-ddTHH:mm:ss")
            );
        }
    }
}