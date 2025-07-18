package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.OpenMatch;
import fpt.aptech.management_field.payload.dtos.OpenMatchDto;
import fpt.aptech.management_field.payload.request.CreateOpenMatchRequest;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import fpt.aptech.management_field.services.OpenMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/open-matches")
public class OpenMatchController {

    @Autowired
    private OpenMatchService openMatchService;

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
        
        List<OpenMatchDto> openMatches;
        if (sportType != null && !sportType.isEmpty()) {
            openMatches = openMatchService.getOpenMatchesBySport(sportType);
        } else {
            openMatches = openMatchService.getAllOpenMatches();
        }
        
        return ResponseEntity.ok(openMatches);
    }
    
    @GetMapping("/ranked")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<List<OpenMatchDto>> getRankedOpenMatches(
            @RequestParam(required = false) String sportType) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        List<OpenMatchDto> rankedMatches = openMatchService.getRankedOpenMatches(userDetails.getId(), sportType);
        return ResponseEntity.ok(rankedMatches);
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
            OpenMatchDto openMatch = openMatchService.getOpenMatchByBooking(bookingId);
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
}