package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.ParticipatingTeam;
import fpt.aptech.management_field.models.Payment;
import fpt.aptech.management_field.payload.dtos.TournamentDto;

import fpt.aptech.management_field.payload.request.TournamentRegistrationRequest;
import fpt.aptech.management_field.services.ParticipatingTeamService;
import fpt.aptech.management_field.services.PaymentService;
import fpt.aptech.management_field.services.TournamentService;
import fpt.aptech.management_field.services.PayPalPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tournament")
public class TournamentController {
    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ParticipatingTeamService participatingTeamService;

    @GetMapping
    public ResponseEntity<List<TournamentDto>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> registerTeamForTournament(
            @RequestBody TournamentRegistrationRequest request,
            @RequestParam(value = "clientType", defaultValue = "web") String clientType) {
        Map<String, Object> response = tournamentService.registerTeamForTournament(request, clientType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<TournamentDto> getTournamentBySlug(@PathVariable String slug) {
        TournamentDto tournamentDto = tournamentService.getTournamentBySlug(slug);
        return ResponseEntity.ok(tournamentDto);
    }

    @GetMapping("/receipt")
    @Transactional
    public RedirectView handleRegister(@RequestParam("participantId") Long participantId, @RequestParam("paymentId") Long paymentId, @RequestParam("status") String status) {
        if (status.equals("success")) {
            ParticipatingTeam participant = participatingTeamService.confirmRegistration(participantId);
            Payment payment = paymentService.getPayment(paymentId);
            participant.setEntryPayment(payment);
            participatingTeamService.save(participant);
            // Redirect to public receipt page (no authentication required)
            return new RedirectView("http://localhost:3000/en/tournament/receipt/" + participant.getTournament().getTournamentId() + "?status=success");
        }
        return new RedirectView("http://localhost:3000/en/tournament/register/failure");
    }

    // Public endpoint to get tournament registration details for receipt page (used by PayPal callback)
    @GetMapping("/public-receipt/{tournamentId}")
    public ResponseEntity<?> getTournamentPublicReceipt(@PathVariable Long tournamentId) {
        try {
            Map<String, Object> receiptData = tournamentService.getTournamentPublicReceipt(tournamentId);
            return ResponseEntity.ok(Map.of(
                "tournament", receiptData.get("tournament"),
                "participation", receiptData.get("participation"),
                "message", "Tournament receipt retrieved successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to fetch tournament receipt: " + e.getMessage(),
                "success", false
            ));
        }
    }

    // Authenticated endpoint to get tournament registration details for receipt page
    @GetMapping("/receipt/{tournamentId}")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTournamentReceipt(@PathVariable Long tournamentId) {
        try {
            Map<String, Object> receiptData = tournamentService.getTournamentReceipt(tournamentId);
            return ResponseEntity.ok(Map.of(
                "tournament", receiptData.get("tournament"),
                "participation", receiptData.get("participation"),
                "message", "Tournament receipt retrieved successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to fetch tournament receipt: " + e.getMessage(),
                "success", false
            ));
        }
    }
}
