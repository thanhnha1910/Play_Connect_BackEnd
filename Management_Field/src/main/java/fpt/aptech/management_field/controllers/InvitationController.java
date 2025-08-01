package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.payload.dtos.InvitationDto;
import fpt.aptech.management_field.payload.dtos.SendInvitationRequest;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import fpt.aptech.management_field.services.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class InvitationController {
    
    @Autowired
    private InvitationService invitationService;
    
    @PostMapping("/invitations")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> sendInvitation(@RequestBody SendInvitationRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            InvitationDto invitation = invitationService.sendInvitation(request, userDetails.getId());
            return ResponseEntity.ok(invitation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Error sending invitation: " + e.getMessage()));
        }
    }
    
    @PostMapping("/open-matches/{matchId}/join-request")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> sendJoinRequest(@PathVariable Long matchId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            InvitationDto joinRequest = invitationService.sendJoinRequest(matchId, userDetails.getId());
            return ResponseEntity.ok(joinRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Error sending join request: " + e.getMessage()));
        }
    }
    
    // UNIFIED ENDPOINTS - Single source of truth for frontend
    
    @GetMapping("/invitations/received")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllReceivedItems() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // This now returns both invitations and join requests in a single call
            List<InvitationDto> allReceived = invitationService.getAllReceivedItems(userDetails.getId());
            return ResponseEntity.ok(allReceived);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Error fetching received items: " + e.getMessage()));
        }
    }
    
    @GetMapping("/invitations/sent")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllSentItems() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // This now returns both invitations and join requests in a single call
            List<InvitationDto> allSent = invitationService.getAllSentItems(userDetails.getId());
            return ResponseEntity.ok(allSent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Error fetching sent items: " + e.getMessage()));
        }
    }
    
    @PostMapping("/invitations/{invitationId}/accept")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> acceptInvitation(@PathVariable Long invitationId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            InvitationDto invitation = invitationService.acceptInvitation(invitationId, userDetails.getId());
            return ResponseEntity.ok(invitation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Error accepting invitation: " + e.getMessage()));
        }
    }
    
    @PostMapping("/invitations/{invitationId}/reject")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> rejectInvitation(@PathVariable Long invitationId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            InvitationDto invitation = invitationService.rejectInvitation(invitationId, userDetails.getId());
            return ResponseEntity.ok(invitation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Error rejecting invitation: " + e.getMessage()));
        }
    }
    
    // DEPRECATED ENDPOINTS - These are no longer needed as data is now unified
    // Keeping them temporarily for backward compatibility, but they should be removed in future versions
    
    @Deprecated
    @GetMapping("/invitations/join-requests/received")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> getReceivedJoinRequests() {
        // Redirect to unified endpoint
        return getAllReceivedItems();
    }
    
    @Deprecated
    @GetMapping("/invitations/join-requests/sent")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> getSentJoinRequests() {
        // Redirect to unified endpoint
        return getAllSentItems();
    }
    
    @PostMapping("/invitations/join-requests/{requestId}/accept")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> acceptJoinRequest(@PathVariable Long requestId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            InvitationDto joinRequest = invitationService.acceptJoinRequest(requestId, userDetails.getId());
            return ResponseEntity.ok(joinRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Error accepting join request: " + e.getMessage()));
        }
    }
    
    @PostMapping("/invitations/join-requests/{requestId}/reject")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> rejectJoinRequest(@PathVariable Long requestId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            InvitationDto joinRequest = invitationService.rejectJoinRequest(requestId, userDetails.getId());
            return ResponseEntity.ok(joinRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Error rejecting join request: " + e.getMessage()));
        }
    }
}