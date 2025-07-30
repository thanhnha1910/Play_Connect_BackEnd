package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.payload.dtos.TournamentDto;

import fpt.aptech.management_field.payload.request.TournamentRegistrationRequest;
import fpt.aptech.management_field.services.TournamentService;
import fpt.aptech.management_field.services.PayPalPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {
    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private PayPalPaymentService payPalPaymentService;

    @GetMapping
    public ResponseEntity<List<TournamentDto>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> registerTeamForTournament(@RequestBody TournamentRegistrationRequest request) {
        try {
            Map<String, Object> response = tournamentService.registerTeamForTournament(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/paypal/success")
    public void paypalSuccess(
            @RequestParam("token") String token,
            @RequestParam("PayerID") String payerId,
            @RequestParam("teamId") Long teamId,
            HttpServletResponse response) throws IOException {
        try {
            payPalPaymentService.capturePayment(teamId, token, payerId);
            tournamentService.confirmRegistration(teamId, token, payerId);
            String redirectUrl = String.format("http://localhost:3000/en/tournament/success?teamId=%d&status=success", teamId);
            response.sendRedirect(redirectUrl);
        } catch (RuntimeException e) {
            String errorRedirectUrl = "http://localhost:3000/en/tournament/failure?error=" + e.getMessage();
            response.sendRedirect(errorRedirectUrl);
        }
    }

    @GetMapping("/paypal/cancel")
    public void paypalCancel(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        // Handle cancelled payment, e.g., update booking status to cancelled or pending
        String redirectUrl = "http://localhost:3000/en/tournament/cancel?token=" + token;
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<TournamentDto> getTournamentBySlug(@PathVariable String slug) {
        TournamentDto tournamentDto = tournamentService.getTournamentBySlug(slug);
        return ResponseEntity.ok(tournamentDto);
    }
}
