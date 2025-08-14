package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.Notification;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewNotificationScheduler {
    
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;
    
    // Run every 5 minutes
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    @Transactional
    public void checkCompletedBookingsForReview() {
        log.info("Checking completed bookings for review notifications...");
        
        try {
            // Find bookings that are completed and ended more than 30 minutes ago
            Instant thirtyMinutesAgo = Instant.now().minusSeconds(30 * 60);
            
            // Get all confirmed bookings that have ended
            List<Booking> completedBookings = bookingRepository.findCompletedBookingsForReview(thirtyMinutesAgo);
            
            log.info("Found {} completed bookings to check for review notifications", completedBookings.size());
            
            for (Booking booking : completedBookings) {
                // Check if user hasn't reviewed this booking yet
                if (!reviewRepository.existsByBooking_BookingId(booking.getBookingId())) {
                    // Check if we haven't already sent a review notification for this booking
                    if (!hasReviewNotificationBeenSent(booking.getBookingId())) {
                        sendReviewNotification(booking);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error checking completed bookings for review: ", e);
        }
    }
    
    private boolean hasReviewNotificationBeenSent(Long bookingId) {
        // Check if a review notification has already been sent for this booking
        // We need to check in the notification repository directly
        try {
            // For now, we'll use a simple approach - check if we've processed this booking before
            // This could be enhanced by adding a notification history table or flag
            return false; // Allow sending notification for now, can be enhanced later
        } catch (Exception e) {
            log.error("Error checking review notification status for booking {}: ", bookingId, e);
            return false;
        }
    }
    
    private void sendReviewNotification(Booking booking) {
        try {
            log.info("Sending review notification for booking: {}", booking.getBookingId());
            
            // Use the new method from NotificationService
            notificationService.createReviewNotificationForUser(
                booking.getUser(), 
                booking.getField().getName(), 
                booking.getBookingId()
            );
            
            log.info("Review notification sent successfully for booking: {}", booking.getBookingId());
            
        } catch (Exception e) {
            log.error("Failed to send review notification for booking {}: ", booking.getBookingId(), e);
        }
    }
}