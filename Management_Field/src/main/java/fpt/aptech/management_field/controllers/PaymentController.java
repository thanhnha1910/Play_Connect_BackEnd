package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.Payment;
import fpt.aptech.management_field.models.ParticipatingTeam;
import fpt.aptech.management_field.services.BookingService;
import fpt.aptech.management_field.services.ParticipatingTeamService;
import fpt.aptech.management_field.services.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private ParticipatingTeamService participatingTeamService;

    @GetMapping("/paypal/callback")
    public RedirectView handlePayPalCallback(@RequestParam String paymentId) {
        try {
            // First, handle the payment callback
            Payment callbackPayment = paymentService.handlePayPalCallback(Long.parseLong(paymentId));
            
            // Then, update the corresponding booking or tournament status
            if ("BOOKING".equals(callbackPayment.getPayableType().name())) {
                // Update booking status to confirmed
                try {
                    bookingService.confirmPayment(callbackPayment.getPayableId(), null, null);
                    logger.info("Booking {} status updated to confirmed after payment success", callbackPayment.getPayableId());
                } catch (Exception e) {
                    logger.error("Failed to update booking status for booking {}: {}", callbackPayment.getPayableId(), e.getMessage());
                }
                String redirectUrl = String.format("http://localhost:3000/en/booking/receipt/%d?status=success", callbackPayment.getPayableId());
                return new RedirectView(redirectUrl);
            } else if ("TOURNAMENT".equals(callbackPayment.getPayableType().name())) {
                // Update tournament participation status to confirmed
                try {
                    ParticipatingTeam participant = participatingTeamService.confirmRegistration(callbackPayment.getPayableId());
                    logger.info("Tournament participation {} status updated to confirmed after payment success", callbackPayment.getPayableId());
                    // Use tournament ID from the participant for redirect
                    Long tournamentId = participant.getTournament().getTournamentId();
                    String redirectUrl = String.format("http://localhost:3000/en/tournament/receipt/%d?status=success", tournamentId);
                    return new RedirectView(redirectUrl);
                } catch (Exception e) {
                    logger.error("Failed to update tournament participation status for participant {}: {}", callbackPayment.getPayableId(), e.getMessage());
                    // Fallback redirect on error
                    return new RedirectView("http://localhost:3000/en/payment/cancel?error=tournament_update_failed&message=Failed-to-update-tournament-status");
                }
            } else {
                // Fallback for other types
                String redirectUrl = "http://localhost:3000/en/payment/success";
                return new RedirectView(redirectUrl);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid paymentId format: {}", paymentId, e);
            return new RedirectView("http://localhost:3000/en/payment/cancel?error=invalid_payment_id&message=Invalid-payment-ID");
        } catch (Exception e) {
            logger.error("Error processing PayPal callback", e);
            return new RedirectView("http://localhost:3000/en/payment/cancel?error=payment_failed&message=Payment-processing-error");
        }
    }

    @GetMapping("/paypal/cancel")
    public RedirectView paypalCancel(@RequestParam String paymentId) {
        try {
            Payment cancelPayment = paymentService.handlePayPalCancel(Long.parseLong(paymentId));
            return new RedirectView("http://localhost:3000/en/payment/cancel?paymentId=" + cancelPayment.getPaymentId());
        } catch (NumberFormatException e) {
            logger.error("Invalid paymentId format: {}", paymentId, e);
            return new RedirectView("http://localhost:3000/en/payment/cancel?error=invalid_payment_id&message=Invalid-paymentId");
        }
    }
}
