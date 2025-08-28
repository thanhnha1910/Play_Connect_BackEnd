package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.Payment;
import fpt.aptech.management_field.models.ParticipatingTeam;
import fpt.aptech.management_field.models.Booking;
import java.util.List;
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
    public RedirectView handlePayPalCallback(
            @RequestParam String paymentId,
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String PayerID,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        try {
            logger.info("PayPal callback received - paymentId: {}, token: {}, PayerID: {}", paymentId, token, PayerID);
            
            // First, handle the payment callback
            Payment callbackPayment = paymentService.handlePayPalCallback(Long.parseLong(paymentId));
            
            // Detect if request is from Flutter app (mobile)
            boolean isFlutterApp = userAgent != null && 
                (userAgent.toLowerCase().contains("flutter") || 
                 userAgent.toLowerCase().contains("dart") ||
                 userAgent.toLowerCase().contains("android") ||
                 userAgent.toLowerCase().contains("ios"));
            
            logger.info("Request from Flutter app: {}, User-Agent: {}", isFlutterApp, userAgent);
            
            // Then, update the corresponding booking or tournament status
            if ("BOOKING".equals(callbackPayment.getPayableType().name())) {
                // Update booking status to confirmed (handle both single and batch bookings)
                try {
                    // Try batch confirmation first (this will handle multiple bookings created together)
                    List<Booking> confirmedBookings = bookingService.confirmBatchPayment(callbackPayment.getPayableId());
                    if (confirmedBookings.size() > 1) {
                        logger.info("Batch booking confirmation: {} bookings confirmed after payment success", confirmedBookings.size());
                    } else {
                        logger.info("Single booking {} status updated to confirmed after payment success", callbackPayment.getPayableId());
                    }
                } catch (Exception e) {
                    logger.error("Failed to update booking status for booking {}: {}", callbackPayment.getPayableId(), e.getMessage());
                }
                
                // Choose redirect URL based on client type
                String redirectUrl;
                if (isFlutterApp) {
                    // For Flutter app, redirect to a custom scheme or special endpoint
                    redirectUrl = String.format("playerconnect://payment/success?bookingId=%d&paymentId=%s&token=%s&PayerID=%s", 
                        callbackPayment.getPayableId(), paymentId, token != null ? token : "", PayerID != null ? PayerID : "");
                } else {
                    // For web frontend
                    redirectUrl = String.format("http://localhost:3000/en/booking/receipt/%d?status=success", callbackPayment.getPayableId());
                }
                logger.info("Redirecting to: {}", redirectUrl);
                return new RedirectView(redirectUrl);
                
            } else if ("TOURNAMENT".equals(callbackPayment.getPayableType().name())) {
                // Update tournament participation status to confirmed
                try {
                    ParticipatingTeam participant = participatingTeamService.confirmRegistration(callbackPayment.getPayableId());
                    // IMPORTANT: Link the payment to the participating team
                    participant.setEntryPayment(callbackPayment);
                    participatingTeamService.save(participant);
                    logger.info("Tournament participation {} status updated to confirmed and payment linked after payment success", callbackPayment.getPayableId());
                    // Use tournament ID from the participant for redirect
                    Long tournamentId = participant.getTournament().getTournamentId();
                    
                    String redirectUrl;
                    if (isFlutterApp) {
                        redirectUrl = String.format("playerconnect://payment/success?tournamentId=%d&paymentId=%s&token=%s&PayerID=%s", 
                            tournamentId, paymentId, token != null ? token : "", PayerID != null ? PayerID : "");
                    } else {
                        redirectUrl = String.format("http://localhost:3000/en/tournament/receipt/%d?status=success", tournamentId);
                    }
                    logger.info("Redirecting to: {}", redirectUrl);
                    return new RedirectView(redirectUrl);
                } catch (Exception e) {
                    logger.error("Failed to update tournament participation status for participant {}: {}", callbackPayment.getPayableId(), e.getMessage());
                    // Fallback redirect on error
                    String errorUrl = isFlutterApp ? 
                        "playerconnect://payment/error?error=tournament_update_failed&message=Failed-to-update-tournament-status" :
                        "http://localhost:3000/en/payment/cancel?error=tournament_update_failed&message=Failed-to-update-tournament-status";
                    return new RedirectView(errorUrl);
                }
            } else {
                // Fallback for other types
                String redirectUrl = isFlutterApp ? 
                    "playerconnect://payment/success" :
                    "http://localhost:3000/en/payment/success";
                return new RedirectView(redirectUrl);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid paymentId format: {}", paymentId, e);
            boolean isFlutterApp = userAgent != null && 
                (userAgent.toLowerCase().contains("flutter") || 
                 userAgent.toLowerCase().contains("dart") ||
                 userAgent.toLowerCase().contains("android") ||
                 userAgent.toLowerCase().contains("ios"));
            String errorUrl = isFlutterApp ?
                "playerconnect://payment/error?error=invalid_payment_id&message=Invalid-payment-ID" :
                "http://localhost:3000/en/payment/cancel?error=invalid_payment_id&message=Invalid-payment-ID";
            return new RedirectView(errorUrl);
        } catch (Exception e) {
            logger.error("Error processing PayPal callback", e);
            boolean isFlutterApp = userAgent != null && 
                (userAgent.toLowerCase().contains("flutter") || 
                 userAgent.toLowerCase().contains("dart") ||
                 userAgent.toLowerCase().contains("android") ||
                 userAgent.toLowerCase().contains("ios"));
            String errorUrl = isFlutterApp ?
                "playerconnect://payment/error?error=payment_failed&message=Payment-processing-error" :
                "http://localhost:3000/en/payment/cancel?error=payment_failed&message=Payment-processing-error";
            return new RedirectView(errorUrl);
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
