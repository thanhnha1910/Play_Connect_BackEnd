package fpt.aptech.management_field.controllers;


import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.BookingDTO;
import fpt.aptech.management_field.payload.dtos.BookingReceiptDTO;
import fpt.aptech.management_field.mappers.BookingMapper;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import fpt.aptech.management_field.services.BookingService;
import fpt.aptech.management_field.services.FieldService;
import fpt.aptech.management_field.services.PayPalPaymentService;
import fpt.aptech.management_field.services.AIRecommendationService;
import fpt.aptech.management_field.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.ArrayList;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    private static final double MINIMUM_COMPATIBILITY_THRESHOLD = 0.2;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private PayPalPaymentService payPalPaymentService;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private AIRecommendationService aiRecommendationService;

    @Autowired
    private UserRepository userRepository;

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

    // DEPRECATED: This endpoint has been removed to eliminate redundant payment capture logic.
    // All payment capture logic is now consolidated in the GET /success endpoint.

    @GetMapping("/payment-callback")
    public ResponseEntity<?> handlePaymentCallback(
            @RequestParam String token,
            @RequestParam String PayerID,
            @RequestParam(required = false) String bookingId) {
        try {
            if (bookingId == null || bookingId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Booking ID is required"
                ));
            }

            Long extractedBookingId = Long.valueOf(bookingId);
            Booking booking = bookingService.getBookingById(extractedBookingId);

            if (booking == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Booking not found"
                ));
            }

            // If booking is already confirmed, just return success with booking details
            if ("confirmed".equals(booking.getStatus())) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "booking", booking,
                        "message", "Payment already completed successfully"
                ));
            }

            // If booking is still pending, process the payment
            if ("pending".equals(booking.getStatus())) {
                booking = bookingService.handlePaymentCallback(token, PayerID, bookingId);
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "booking", booking,
                        "message", "Payment completed successfully"
                ));
            }

            // If booking is in any other state, return error
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Booking is in invalid state: " + booking.getStatus()
            ));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Invalid booking ID format"
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
        String redirectUrl = "http://localhost:3000/en/booking/cancel?bookingId=" +
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
            @RequestParam String token,
            @RequestParam String PayerID,
            @RequestParam String bookingId, // Keep as String for initial validation
            HttpServletResponse response) throws IOException {
        try {
            if (bookingId == null || bookingId.isEmpty()) {
                throw new IllegalArgumentException("Booking ID is required for payment confirmation.");
            }
            Long longBookingId = Long.valueOf(bookingId);

            // --- SINGLE SOURCE OF TRUTH LOGIC ---
            // 1. Capture payment with PayPal
            payPalPaymentService.capturePayment(longBookingId, token, PayerID);

            // 2. Confirm booking in local DB
            bookingService.confirmPayment(longBookingId, token, PayerID);
            // --- END OF LOGIC ---

            // 3. Redirect to a "dumb" frontend receipt page
            String redirectUrl = String.format("http://localhost:3000/en/booking/success?bookingId=%d&status=success", longBookingId);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            System.err.println("Payment success handling failed: " + e.getMessage());
            // Redirect to a failure page with an error message
            String errorRedirectUrl = "http://localhost:3000/en/booking/failure?error=" + e.getMessage();
            response.sendRedirect(errorRedirectUrl);
        }
    }

    // Debug endpoint to check booking status - public access for debugging
    @GetMapping("/debug/{bookingId}")
    public ResponseEntity<?> debugBookingStatus(@PathVariable Long bookingId) {
        try {
            System.out.println("=== DEBUG: Checking booking ID: " + bookingId + " ===");
            Booking booking = bookingService.getBookingById(bookingId);
            System.out.println("=== DEBUG: Booking found: " + (booking != null) + " ===");

            if (booking == null) {
                return ResponseEntity.ok(Map.of(
                        "bookingId", bookingId,
                        "exists", false,
                        "message", "Booking not found"
                ));
            }

            System.out.println("=== DEBUG: Booking status: " + booking.getStatus() + " ===");

            return ResponseEntity.ok(Map.of(
                    "bookingId", booking.getBookingId(),
                    "exists", true,
                    "status", booking.getStatus(),
                    "userId", booking.getUser().getId(),
                    "fieldId", booking.getField().getFieldId(),
                    "fromTime", booking.getFromTime().toString(),
                    "toTime", booking.getToTime().toString(),
                    "paymentToken", booking.getPaymentToken(),
                    "message", "Booking found successfully"
            ));
        } catch (Exception e) {
            System.err.println("=== DEBUG ERROR: " + e.getMessage() + " ===");
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to fetch booking: " + e.getMessage(),
                    "bookingId", bookingId,
                    "stackTrace", e.getClass().getSimpleName()
            ));
        }
    }

    // Authenticated endpoint to get booking details for receipt page
    @GetMapping("/details/{bookingId}")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> getBookingDetails(@PathVariable Long bookingId, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();

            Booking booking = bookingService.getBookingById(bookingId);

            if (booking == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Booking not found",
                        "bookingId", bookingId
                ));
            }

            // Check if the authenticated user owns this booking
            if (!booking.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only view your own booking details"));
            }

            return ResponseEntity.ok(Map.of(
                    "booking", booking,
                    "message", "Booking details retrieved successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to fetch booking details: " + e.getMessage(),
                    "bookingId", bookingId
            ));
        }
    }


    @GetMapping("/receipt/{bookingId}")
    public ResponseEntity<?> getBookingReceipt(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingService.getBookingById(bookingId);

            if (booking == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Booking not found",
                        "bookingId", bookingId
                ));
            }

            // Only allow access to confirmed bookings for security
            if (!"confirmed".equals(booking.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Booking is not confirmed",
                        "status", booking.getStatus(),
                        "bookingId", bookingId
                ));
            }

            // Convert to DTO to avoid Hibernate proxy serialization issues
            BookingReceiptDTO receiptDTO = BookingMapper.mapToReceiptDTO(booking);
            return ResponseEntity.ok(Map.of(
                "booking", receiptDTO,
                "message", "Booking receipt retrieved successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to fetch booking receipt: " + e.getMessage(),
                "bookingId", bookingId
            ));
        }
    }

    // New PayPal callback endpoint - single source of truth for payment processing
    @GetMapping("/paypal/callback")
    public void handlePayPalCallback(
            @RequestParam String token,
            @RequestParam("PayerID") String payerId,
            @RequestParam String bookingId,
            HttpServletResponse response) throws IOException {
        try {
            Long longBookingId = Long.valueOf(bookingId);

            // --- SINGLE SOURCE OF TRUTH LOGIC ---
            // 1. Capture payment with PayPal
            payPalPaymentService.capturePayment(longBookingId, token, payerId);

            // 2. Confirm booking in local DB
            bookingService.confirmPayment(longBookingId, token, payerId);
            // --- END OF LOGIC ---

            // 3. Redirect to a "dumb" frontend receipt page
            String redirectUrl = String.format("http://localhost:3000/en/booking/receipt/%d?status=success", longBookingId);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            System.err.println("PayPal callback handling failed: " + e.getMessage());
            String errorRedirectUrl = "http://localhost:3000/en/booking/failure?error=" + e.getMessage();
            response.sendRedirect(errorRedirectUrl);
        }
    }

    // DEPRECATED: Old PayPal capture endpoint - kept for backward compatibility but should not be used
    @PostMapping("/paypal/capture")
    @Deprecated
    public ResponseEntity<?> capturePayPalPayment(@RequestBody Map<String, String> request) {
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of(
            "status", "error",
            "message", "This endpoint is deprecated. Payment processing is now handled automatically via PayPal callback."
        ));
    }

    // Recommend teammates for a specific booking
    @GetMapping("/{bookingId}/recommend-teammates")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> recommendTeammates(@PathVariable Long bookingId, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            Booking booking = bookingService.getBookingById(bookingId);
            
            if (booking == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Booking not found",
                    "bookingId", bookingId
                ));
            }
            
            // Check if the authenticated user owns this booking
            if (!booking.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only get recommendations for your own bookings"));
            }
            
            // Get the current user
            User currentUser = booking.getUser();
            
            // Get potential teammates (discoverable ROLE_USER only, excluding current user)
            List<User> potentialTeammates = userRepository.findDiscoverableRegularUsersExcludingUser(userId);
            
            // Determine sport type from booking (you might need to add this field to Booking)
            // For now, we'll use a default sport type or extract from field name
            String sportType = "BONG_DA"; // Default to football/soccer
            
            List<Map<String, Object>> recommendations;
            
            // Always try to use AI service for real recommendations
            try {
                // Try hybrid recommendation first, fallback to legacy if needed
                try {
                    recommendations = aiRecommendationService.recommendTeammatesHybrid(currentUser, potentialTeammates, sportType);
                    logger.info("Using hybrid recommendation model for booking {}", bookingId);
                } catch (Exception hybridException) {
                    logger.warn("Hybrid recommendation failed for booking {}, falling back to legacy: {}", bookingId, hybridException.getMessage());
                    recommendations = aiRecommendationService.recommendTeammates(currentUser, potentialTeammates, sportType);
                }
                
                // Validate that AI service returned proper scores
                boolean hasValidScores = recommendations.stream()
                    .anyMatch(rec -> rec.containsKey("compatibilityScore") && 
                             rec.get("compatibilityScore") instanceof Number &&
                             ((Number) rec.get("compatibilityScore")).doubleValue() != 0.5);
                
                if (!hasValidScores) {
                    throw new RuntimeException("AI service returned invalid or mock scores");
                }

                // --- FILTER OUT LOW COMPATIBILITY SCORES ---
                List<Map<String, Object>> filteredRecommendations = recommendations.stream()
                    .filter(rec -> {
                        if (rec.containsKey("compatibilityScore") && rec.get("compatibilityScore") instanceof Number) {
                            double score = ((Number) rec.get("compatibilityScore")).doubleValue();
                            return score >= MINIMUM_COMPATIBILITY_THRESHOLD;
                        }
                        return false; // Exclude recommendations without valid compatibility scores
                    })
                    .collect(Collectors.toList());

                recommendations = filteredRecommendations;
                logger.info("Filtered recommendations: {} out of {} teammates meet minimum compatibility threshold of {}",
                           recommendations.size(), potentialTeammates.size(), MINIMUM_COMPATIBILITY_THRESHOLD);
                // --- END OF FILTERING LOGIC ---

            } catch (Exception e) {
                logger.error("AI service failed, returning error instead of mock data: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "error", "AI recommendation service is currently unavailable",
                    "details", e.getMessage(),
                    "bookingId", bookingId,
                    "aiServiceAvailable", false
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "bookingId", bookingId,
                "sportType", sportType,
                "recommendations", recommendations,
                "totalRecommendations", recommendations.size(),
                "aiServiceAvailable", aiRecommendationService.isAIServiceAvailable(),
                "message", "Teammate recommendations retrieved successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to get teammate recommendations: " + e.getMessage(),
                "bookingId", bookingId
            ));
        }
    }

    // Simple test endpoint
    @GetMapping("/test-endpoint")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(Map.of(
            "message", "Test endpoint working",
            "timestamp", System.currentTimeMillis()
        ));
    }

    // Convert draft match to real booking
    @PostMapping("/from-draft/{draftMatchId}")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> convertDraftMatchToBooking(
            @PathVariable Long draftMatchId,
            @RequestParam Long bookingId,
            Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();

            Map<String, Object> result = bookingService.convertDraftMatchToBooking(draftMatchId, bookingId, userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Draft match converted to booking successfully",
                "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
