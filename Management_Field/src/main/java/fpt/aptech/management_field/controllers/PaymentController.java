package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.Payment;
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

    @GetMapping("/paypal/callback")
    public RedirectView handlePayPalCallback(@RequestParam String paymentId) {
        try {
            Payment callbackPayment = paymentService.handlePayPalCallback(Long.parseLong(paymentId));
            String redirectUrl = String.format("http://localhost:1444/api/%s/receipt?participantId=%d&paymentId=%d&status=success", callbackPayment.getPayableType().name().toLowerCase(), callbackPayment.getPayableId(), callbackPayment.getPaymentId());
            return new RedirectView(redirectUrl);
        } catch (NumberFormatException e) {
            logger.error("Invalid paymentId format: {}", paymentId, e);
            return new RedirectView("http://localhost:3000/en/payment/receipt/error?message=Invalid-payment-ID");
        } catch (Exception e) {
            logger.error("Error processing PayPal callback", e);
            return new RedirectView("http://localhost:3000/en/payment/receipt/error?message=Payment-processing-error");
        }
    }

    @GetMapping("/paypal/cancel")
    public RedirectView paypalCancel(@RequestParam String paymentId) {
        try {
            Payment cancelPayment = paymentService.handlePayPalCancel(Long.parseLong(paymentId));
            return new RedirectView("http://localhost:3000/en/payment/cancel?paymentId=" + cancelPayment.getPaymentId());
        } catch (NumberFormatException e) {
            logger.error("Invalid paymentId format: {}", paymentId, e);
            return new RedirectView("http://localhost:3000/en/payment/receipt/error?message=Invalid-paymentId");
        }
    }
}
