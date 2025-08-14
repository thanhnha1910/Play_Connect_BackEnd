package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.Location;
import fpt.aptech.management_field.models.Owner;
import fpt.aptech.management_field.models.ParticipatingTeam;
import fpt.aptech.management_field.models.PaymentStatus;
import fpt.aptech.management_field.models.Tournament;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.LocationDto;
import fpt.aptech.management_field.payload.dtos.PaginationDto;
import fpt.aptech.management_field.payload.dtos.TeamDto;
import fpt.aptech.management_field.payload.dtos.TournamentDto;
import fpt.aptech.management_field.payload.request.CreateTournamentRequest;
import fpt.aptech.management_field.payload.request.UpdateTournamentRequest;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.payload.response.TournamentDetailResponse;
import fpt.aptech.management_field.repositories.LocationRepository;
import fpt.aptech.management_field.repositories.OwnerRepository;
import fpt.aptech.management_field.repositories.ParticipatingTeamRepository;
import fpt.aptech.management_field.repositories.TournamentRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import fpt.aptech.management_field.services.TournamentService;
import fpt.aptech.management_field.services.NotificationService;
import fpt.aptech.management_field.mappers.TournamentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/owner")
@PreAuthorize("hasRole('OWNER')")
public class OwnerTournamentController {

    @Autowired
    private TournamentRepository tournamentRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private ParticipatingTeamRepository participatingTeamRepository;
    
    @Autowired
    private OwnerRepository ownerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TournamentService tournamentService;
    
    @Autowired
    private TournamentMapper tournamentMapper;

    /**
     * Get all tournaments for owner with filters and pagination
     */
    @GetMapping("/tournaments")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOwnerTournaments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long locationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Owner owner = getOwnerByUser(currentUser);
            
            // Get all tournaments and filter by location owner
            List<Tournament> tournaments = tournamentRepository.findAll();
            
            // Filter by location owner
            tournaments = tournaments.stream()
                .filter(t -> t.getLocation() != null && 
                           t.getLocation().getOwner() != null && 
                           t.getLocation().getOwner().getOwnerId().equals(owner.getOwnerId()))
                .collect(Collectors.toList());
            
            // Apply status filter if provided
            if (status != null && !status.isEmpty()) {
                tournaments = tournaments.stream()
                    .filter(t -> status.equals(t.getStatus()))
                    .collect(Collectors.toList());
            }
            
            // Apply location filter if provided
            if (locationId != null) {
                tournaments = tournaments.stream()
                    .filter(t -> t.getLocation() != null && 
                               locationId.equals(t.getLocation().getLocationId()))
                    .collect(Collectors.toList());
            }
            
            // Convert to DTOs
            List<TournamentDto> tournamentDtos = tournaments.stream()
                .map(tournamentMapper::toDto)
                .collect(Collectors.toList());
            
            // Apply pagination manually
            int start = (page - 1) * size;
            int end = Math.min(start + size, tournamentDtos.size());
            List<TournamentDto> pageContent = tournamentDtos.subList(start, end);
            
            // Create pagination info
            PaginationDto pagination = new PaginationDto(
                tournamentDtos.size(),
                (int) Math.ceil((double) tournamentDtos.size() / size),
                page,
                size
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("tournaments", pageContent);
            response.put("pagination", pagination);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get single tournament by ID with details
     */
    @GetMapping("/tournaments/{tournamentId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTournamentDetail(
            @PathVariable Long tournamentId,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Owner owner = getOwnerByUser(currentUser);
            
            Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
            
            // Verify owner has access to this tournament
            if (tournament.getLocation().getOwner() == null || 
                !tournament.getLocation().getOwner().getOwnerId().equals(owner.getOwnerId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Access denied: Tournament does not belong to this owner"));
            }
            
            // Get participating teams
            List<ParticipatingTeam> participatingTeams = participatingTeamRepository.findByTournamentId(tournamentId);
            
            // Build response
            TournamentDetailResponse response = new TournamentDetailResponse();
            response.setTournamentId(tournament.getTournamentId());
            response.setName(tournament.getName());
            response.setSlug(tournament.getSlug());
            response.setDescription(tournament.getDescription());
            response.setStartDate(tournament.getStartDate());
            response.setEndDate(tournament.getEndDate());
            response.setPrize(tournament.getPrize());
            response.setEntryFee(tournament.getEntryFee());
            response.setSlots(tournament.getSlots());
            response.setStatus(tournament.getStatus());
            response.setLocation(tournamentMapper.toLocationDto(tournament.getLocation()));
            
            // Map participating teams
            List<TournamentDetailResponse.ParticipatingTeamInfo> teamInfos = participatingTeams.stream()
                .map(pt -> {
                    TournamentDetailResponse.ParticipatingTeamInfo info = new TournamentDetailResponse.ParticipatingTeamInfo();
                    info.setParticipatingTeamId(pt.getParticipatingTeamId());
                    info.setTeam(tournamentMapper.toTeamDto(pt.getTeam()));
                    info.setStatus(pt.getStatus());
                    // Add payment information if available
                    if (pt.getEntryPayment() != null && pt.getEntryPayment().getStatus() == PaymentStatus.SUCCESS) {
                        info.setPaymentDate(pt.getEntryPayment().getUpdatedAt()); // Use updatedAt for actual payment completion date
                        info.setPaymentStatus("completed");
                    } else {
                        info.setPaymentStatus("pending");
                        info.setPaymentDate(null);
                    }
                    return info;
                })
                .collect(Collectors.toList());
            response.setParticipatingTeams(teamInfos);
            
            // Calculate financial summary
            TournamentDetailResponse.FinancialSummary financialSummary = new TournamentDetailResponse.FinancialSummary();
            int paidTeams = (int) participatingTeams.stream()
                .filter(pt -> pt.getEntryPayment() != null && pt.getEntryPayment().getStatus() == PaymentStatus.SUCCESS)
                .count();
            financialSummary.setRegisteredTeams(participatingTeams.size());
            financialSummary.setPaidTeams(paidTeams);
            financialSummary.setPendingPayments(participatingTeams.size() - paidTeams);
            financialSummary.setTotalRevenue(tournament.getEntryFee() * paidTeams);
            financialSummary.setTotalExpenses(tournament.getPrize());
            financialSummary.setNetProfit(financialSummary.getTotalRevenue() - financialSummary.getTotalExpenses());
            response.setFinancialSummary(financialSummary);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Create new tournament
     */
    @PostMapping("/tournaments")
    @Transactional
    public ResponseEntity<?> createTournament(
            @Valid @RequestBody CreateTournamentRequest request,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Owner owner = getOwnerByUser(currentUser);
            
            // Verify location belongs to owner
            Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));
            
            if (location.getOwner() == null || 
                !location.getOwner().getOwnerId().equals(owner.getOwnerId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Access denied: Location does not belong to this owner"));
            }
            
            // Validate dates
            if (request.getEndDate().isBefore(request.getStartDate())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("End date must be after start date"));
            }
            
            // Create tournament
            Tournament tournament = new Tournament();
            tournament.setName(request.getName());
            tournament.setDescription(request.getDescription());
            tournament.setStartDate(request.getStartDate());
            tournament.setEndDate(request.getEndDate());
            tournament.setPrize(request.getPrize());
            tournament.setEntryFee(request.getEntryFee());
            tournament.setSlots(request.getSlots());
            tournament.setStatus("upcoming");
            tournament.setLocation(location);
            
            // Generate slug from name
            String slug = request.getName().toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
            tournament.setSlug(slug);
            
            Tournament savedTournament = tournamentRepository.save(tournament);
            
            // Send notification to field owner
            try {
                User fieldOwner = location.getOwner().getUser();
                if (fieldOwner != null) {
                    String startDate = savedTournament.getStartDate().toString();
                    notificationService.createTournamentNotificationForOwner(
                        fieldOwner, 
                        savedTournament.getName(), 
                        location.getName(), 
                        startDate, 
                        savedTournament.getTournamentId()
                    );
                }
            } catch (Exception e) {
                // Log error but don't fail tournament creation
                System.err.println("Failed to send tournament notification: " + e.getMessage());
            }
            
            TournamentDto tournamentDto = tournamentMapper.toDto(savedTournament);
            
            return ResponseEntity.ok(tournamentDto);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Update tournament
     */
    @PutMapping("/tournaments/{tournamentId}")
    @Transactional
    public ResponseEntity<?> updateTournament(
            @PathVariable Long tournamentId,
            @Valid @RequestBody UpdateTournamentRequest request,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Owner owner = getOwnerByUser(currentUser);
            
            Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
            
            // Verify owner has access to this tournament
            if (tournament.getLocation().getOwner() == null || 
                !tournament.getLocation().getOwner().getOwnerId().equals(owner.getOwnerId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Access denied: Tournament does not belong to this owner"));
            }
            
            // Update fields if provided
            if (request.getName() != null) {
                tournament.setName(request.getName());
                // Update slug if name changed
                String slug = request.getName().toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")
                    .replaceAll("\\s+", "-");
                tournament.setSlug(slug);
            }
            if (request.getDescription() != null) {
                tournament.setDescription(request.getDescription());
            }
            if (request.getStartDate() != null) {
                tournament.setStartDate(request.getStartDate());
            }
            if (request.getEndDate() != null) {
                tournament.setEndDate(request.getEndDate());
            }
            if (request.getPrize() != null) {
                tournament.setPrize(request.getPrize());
            }
            if (request.getEntryFee() != null) {
                tournament.setEntryFee(request.getEntryFee());
            }
            if (request.getSlots() != null) {
                tournament.setSlots(request.getSlots());
            }
            if (request.getStatus() != null) {
                tournament.setStatus(request.getStatus());
            }
            if (request.getLocationId() != null) {
                Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Location not found"));
                if (location.getOwner() == null || 
                    !location.getOwner().getOwnerId().equals(owner.getOwnerId())) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Access denied: Location does not belong to this owner"));
                }
                tournament.setLocation(location);
            }
            
            // Validate dates if both are present
            if (tournament.getEndDate().isBefore(tournament.getStartDate())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("End date must be after start date"));
            }
            
            Tournament savedTournament = tournamentRepository.save(tournament);
            TournamentDto tournamentDto = tournamentMapper.toDto(savedTournament);
            
            return ResponseEntity.ok(tournamentDto);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Delete/Cancel tournament
     */
    @DeleteMapping("/tournaments/{tournamentId}")
    @Transactional
    public ResponseEntity<?> deleteTournament(
            @PathVariable Long tournamentId,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Owner owner = getOwnerByUser(currentUser);
            
            Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
            
            // Verify owner has access to this tournament
            if (tournament.getLocation().getOwner() == null || 
                !tournament.getLocation().getOwner().getOwnerId().equals(owner.getOwnerId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Access denied: Tournament does not belong to this owner"));
            }
            
            // Check if tournament can be deleted (no participating teams with payments)
            List<ParticipatingTeam> participatingTeams = participatingTeamRepository.findByTournamentId(tournamentId);
            boolean hasPayments = participatingTeams.stream().anyMatch(pt -> pt.getEntryPayment() != null);
            
            if (hasPayments) {
                // If there are payments, just cancel the tournament instead of deleting
                tournament.setStatus("cancelled");
                tournamentRepository.save(tournament);
                return ResponseEntity.ok(new MessageResponse("Tournament cancelled successfully!"));
            } else {
                // Safe to delete if no payments
                tournamentRepository.delete(tournament);
                return ResponseEntity.ok(new MessageResponse("Tournament deleted successfully!"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Update tournament status
     */
    @PatchMapping("/tournaments/{tournamentId}/status")
    @Transactional
    public ResponseEntity<?> updateTournamentStatus(
            @PathVariable Long tournamentId,
            @RequestBody Map<String, String> statusRequest,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Owner owner = getOwnerByUser(currentUser);
            
            Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
            
            // Verify owner has access to this tournament
            if (tournament.getLocation().getOwner() == null || 
                !tournament.getLocation().getOwner().getOwnerId().equals(owner.getOwnerId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Access denied: Tournament does not belong to this owner"));
            }
            
            String newStatus = statusRequest.get("status");
            if (newStatus == null || newStatus.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Status is required"));
            }
            
            tournament.setStatus(newStatus);
            Tournament savedTournament = tournamentRepository.save(tournament);
            TournamentDto tournamentDto = tournamentMapper.toDto(savedTournament);
            
            return ResponseEntity.ok(tournamentDto);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get tournament statistics
     */
    @GetMapping("/tournaments/stats")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTournamentStats(
            @RequestParam(required = false) Long tournamentId,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Owner owner = getOwnerByUser(currentUser);
            
            Map<String, Object> stats = new HashMap<>();
            
            if (tournamentId != null) {
                // Get stats for specific tournament
                Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Tournament not found"));
                
                // Verify owner has access
                if (tournament.getLocation().getOwner() == null || 
                    !tournament.getLocation().getOwner().getOwnerId().equals(owner.getOwnerId())) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Access denied: Tournament does not belong to this owner"));
                }
                
                List<ParticipatingTeam> participatingTeams = participatingTeamRepository.findByTournamentId(tournamentId);
                int paidTeams = (int) participatingTeams.stream().filter(pt -> pt.getEntryPayment() != null).count();
                
                stats.put("registeredTeams", participatingTeams.size());
                stats.put("paidTeams", paidTeams);
                stats.put("pendingPayments", participatingTeams.size() - paidTeams);
                stats.put("totalRevenue", tournament.getEntryFee() * paidTeams);
                stats.put("totalExpenses", tournament.getPrize());
                stats.put("netProfit", (tournament.getEntryFee() * paidTeams) - tournament.getPrize());
            } else {
                // Get overall stats for owner
                List<Tournament> ownerTournaments = tournamentRepository.findAll().stream()
                    .filter(t -> t.getLocation() != null && 
                               t.getLocation().getOwner() != null && 
                               t.getLocation().getOwner().getOwnerId().equals(owner.getOwnerId()))
                    .collect(Collectors.toList());
                
                long totalTournaments = ownerTournaments.size();
                long upcomingTournaments = ownerTournaments.stream()
                    .filter(t -> "upcoming".equals(t.getStatus()))
                    .count();
                long ongoingTournaments = ownerTournaments.stream()
                    .filter(t -> "ongoing".equals(t.getStatus()))
                    .count();
                
                stats.put("totalTournaments", totalTournaments);
                stats.put("upcomingTournaments", upcomingTournaments);
                stats.put("ongoingTournaments", ongoingTournaments);
            }
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get participating teams for a tournament
     */
    @GetMapping("/tournaments/{tournamentId}/teams")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getParticipatingTeams(
            @PathVariable Long tournamentId,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Owner owner = getOwnerByUser(currentUser);
            
            Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
            
            // Verify owner has access to this tournament
            if (tournament.getLocation().getOwner() == null || 
                !tournament.getLocation().getOwner().getOwnerId().equals(owner.getOwnerId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Access denied: Tournament does not belong to this owner"));
            }
            
            List<ParticipatingTeam> participatingTeams = participatingTeamRepository.findByTournamentId(tournamentId);
            
            List<Map<String, Object>> teamInfos = participatingTeams.stream()
                .map(pt -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("participatingTeamId", pt.getParticipatingTeamId());
                    info.put("team", tournamentMapper.toTeamDto(pt.getTeam()));
                    info.put("status", pt.getStatus());
                    // Add registration date from payment creation date (when team registered)
                    if (pt.getEntryPayment() != null) {
                        info.put("registrationDate", pt.getEntryPayment().getCreatedAt());
                    } else {
                        info.put("registrationDate", pt.getTeam().getCreatedAt());
                    }
                    if (pt.getEntryPayment() != null && pt.getEntryPayment().getStatus() == PaymentStatus.SUCCESS) {
                        info.put("paymentDate", pt.getEntryPayment().getUpdatedAt()); // Use updatedAt for actual payment completion date
                        info.put("paymentStatus", "completed");
                    } else {
                        info.put("paymentStatus", "pending");
                        info.put("paymentDate", null);
                    }
                    return info;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(teamInfos);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Get tournament financial data
     */
    @GetMapping("/tournaments/{tournamentId}/financials")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTournamentFinancials(
            @PathVariable Long tournamentId,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Owner owner = getOwnerByUser(currentUser);
            
            Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
            
            // Verify owner has access to this tournament
            if (tournament.getLocation().getOwner() == null || 
                !tournament.getLocation().getOwner().getOwnerId().equals(owner.getOwnerId())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Access denied: Tournament does not belong to this owner"));
            }
            
            List<ParticipatingTeam> participatingTeams = participatingTeamRepository.findByTournamentId(tournamentId);
            // Only count teams with successful PayPal payments
            int paidTeams = (int) participatingTeams.stream()
                .filter(pt -> pt.getEntryPayment() != null && pt.getEntryPayment().getStatus() == PaymentStatus.SUCCESS)
                .count();
            
            Map<String, Object> financials = new HashMap<>();
            financials.put("registeredTeams", participatingTeams.size());
            financials.put("paidTeams", paidTeams);
            financials.put("pendingPayments", participatingTeams.size() - paidTeams);
            financials.put("totalRevenue", tournament.getEntryFee() * paidTeams);
            financials.put("totalExpenses", tournament.getPrize());
            financials.put("netProfit", (tournament.getEntryFee() * paidTeams) - tournament.getPrize());
            financials.put("entryFee", tournament.getEntryFee());
            financials.put("prize", tournament.getPrize());
            
            return ResponseEntity.ok(financials);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    // Helper methods
    private User getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private Owner getOwnerByUser(User user) {
        return ownerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
    }
}