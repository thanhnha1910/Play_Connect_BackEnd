package fpt.aptech.management_field.controllers;


import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.payload.dtos.BookingDTO;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import fpt.aptech.management_field.services.BookingService;
import fpt.aptech.management_field.services.FieldService;
import fpt.aptech.management_field.services.PayPalPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.payload.request.BookingRequest;
import fpt.aptech.management_field.payload.response.MessageResponse;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private PayPalPaymentService payPalPaymentService;

    @GetMapping("/{fieldId}")
    public ResponseEntity<List<BookingDTO>> getBookingsForFieldByDate(@PathVariable("fieldId") Long fieldId, @RequestParam Instant fromDate, @RequestParam Instant toDate) {
        List<BookingDTO> bookingDTOS = bookingService.getBookingsByDate(fromDate, toDate, fieldId);
        return ResponseEntity.ok(bookingDTOS);
    }

    @GetMapping("/available-fields")
    public ResponseEntity<?> getAvailableFields(
            @RequestParam Instant fromTime,
            @RequestParam Instant toTime,
            @RequestParam(required = false) Long locationId) {
        try {
            List<Field> availableFields = fieldService.getAvailableFields(fromTime, toTime, locationId);
            return ResponseEntity.ok(availableFields.isEmpty() ? Map.of("message", "No available fields") : availableFields);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch available fields: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest bookingRequest, Authentication authentication) {
        // Spring Security ensures 'authentication' is not null here because of @PreAuthorize
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        try {
            // The service layer will handle the logic, including payment creation
            Map<String, Object> response = bookingService.createBooking(userId, bookingRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Return a specific error message from the service
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(
            @RequestParam String orderId,
            @RequestParam String token,
            @RequestParam String payerId) {
        try {
            Long bookingId = Long.valueOf(orderId.replace("BOOKING_", ""));
            Booking booking = bookingService.confirmPayment(bookingId, token, payerId);
            return ResponseEntity.ok(Map.of(
                "booking", booking,
                "message", "Payment confirmed successfully",
                "bookingId", booking.getBookingId()
            ));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid order ID format: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid payment data: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to confirm payment: " + e.getMessage()));
        }
    }

    @PostMapping("/capture-paypal-order")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> capturePayPalOrder(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            String token = request.get("token");
            String payerId = request.get("payerId");
            String bookingIdStr = request.get("bookingId");
            
            if (token == null || payerId == null || bookingIdStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required parameters"));
            }
            
            Long bookingId = Long.valueOf(bookingIdStr);
            
            // Verify booking ownership
            Booking existingBooking = bookingService.getBookingById(bookingId);
            if (existingBooking == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Booking not found"));
            }
            
            // Check if the authenticated user owns this booking
            if (!existingBooking.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only capture payment for your own bookings"));
            }
            
            // Capture the payment through PayPal
            payPalPaymentService.capturePayment(bookingId, token, payerId);
            
            // Update booking status
            Booking booking = bookingService.confirmPayment(bookingId, token, payerId);
            
            return ResponseEntity.ok(Map.of(
                "booking", booking,
                "message", "Payment captured successfully",
                "bookingId", booking.getBookingId()
            ));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid booking ID format: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid payment data: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to capture payment: " + e.getMessage()));
        }
    }

    // Public endpoint for PayPal callback - no authentication required
    @PostMapping("/paypal/capture")
    public ResponseEntity<?> capturePayPalPayment(
            @RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String payerId = request.get("payerId");
            String bookingIdStr = request.get("bookingId");
            
            if (token == null || payerId == null || bookingIdStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required parameters"));
            }
            
            Long bookingId = Long.valueOf(bookingIdStr);
            
            // Verify booking exists
            Booking existingBooking = bookingService.getBookingById(bookingId);
            if (existingBooking == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Booking not found"));
            }
            
            // Verify the PayPal order with PayPal API to ensure it's legitimate
            // This is our security layer - we verify with PayPal directly
            boolean isValidPayment = payPalPaymentService.verifyPaymentWithPayPal(token, payerId);
            if (!isValidPayment) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid PayPal payment verification"));
            }
            
            // Capture the payment through PayPal
            payPalPaymentService.capturePayment(bookingId, token, payerId);
            
            // Update booking status
            Booking booking = bookingService.confirmPayment(bookingId, token, payerId);
            
            return ResponseEntity.ok(Map.of(
                "booking", booking,
                "message", "Payment captured successfully",
                "bookingId", booking.getBookingId(),
                "status", "success"
            ));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid booking ID format: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid payment data: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to capture payment: " + e.getMessage()));
        }
    }

    @GetMapping("/payment-callback")
    public ResponseEntity<?> handlePaymentCallback(
            @RequestParam String token,
            @RequestParam String PayerID,
            @RequestParam(required = false) String bookingId) {
        try {
            // Capture the payment and update booking status
            Booking booking = bookingService.handlePaymentCallback(token, PayerID, bookingId);
            
            // Redirect to success page
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "booking", booking,
                "message", "Payment completed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/payment-cancel")
    public void handlePaymentCancel(
            @RequestParam(required = false) String bookingId,
            HttpServletResponse response) throws IOException {
        // Redirect to frontend cancel page
        String redirectUrl = "http://localhost:3000/booking/cancel?bookingId=" + 
                            (bookingId != null ? bookingId : "");
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> getBookingHistory(Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            List<Booking> bookings = bookingService.getBookingHistory(userId);
            return ResponseEntity.ok(bookings.isEmpty() ? Map.of("message", "No bookings found") : bookings);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch booking history: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            Booking booking = bookingService.cancelBooking(userId, bookingId);
            return ResponseEntity.ok(Map.of("message", "Booking canceled", "bookingId", booking.getBookingId()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized to cancel this booking: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to cancel booking: " + e.getMessage()));
        }
    }

    @GetMapping("/success")
    public void handlePaymentSuccess(
            @RequestParam(required = false) String orderId,
            @RequestParam String token,
            @RequestParam String PayerID,
            @RequestParam(required = false) String bookingId,
            HttpServletResponse response) throws IOException {
        try {
            // Validate required parameters
            if (bookingId == null || bookingId.isEmpty()) {
                throw new IllegalArgumentException("Booking ID is required");
            }
            
            Long extractedBookingId = Long.valueOf(bookingId);
            
            // Capture the payment using the token (which is the actual PayPal order ID)
            payPalPaymentService.capturePayment(extractedBookingId, token, PayerID);
            
            // Update booking status to confirmed
            Booking booking = bookingService.confirmPayment(extractedBookingId, token, PayerID);
            
            // Redirect to frontend success page with booking info
            String redirectUrl = String.format(
                "http://localhost:3000/booking/success?token=%s&PayerID=%s&bookingId=%d&status=success",
                token, PayerID, booking.getBookingId()
            );
            response.sendRedirect(redirectUrl);
            
        } catch (NumberFormatException e) {
            System.err.println("Invalid booking ID format: " + e.getMessage());
            response.sendRedirect("http://localhost:3000/booking/cancel?error=invalid_booking_id");
        } catch (Exception e) {
            System.err.println("Payment success handling failed: " + e.getMessage());
            response.sendRedirect("http://localhost:3000/booking/cancel?error=payment_failed&message=" + e.getMessage());
        }
    }
}
