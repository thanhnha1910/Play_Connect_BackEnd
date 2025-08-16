package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.Notification;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.NotificationRepository;
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
    private final NotificationRepository notificationRepository;
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
                    // Check if we haven't already sent a review notification for this booking to this specific user
                    if (!hasReviewNotificationBeenSent(booking.getBookingId(), booking.getUser().getId())) {
                        sendReviewNotification(booking);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error checking completed bookings for review: ", e);
        }
    }
    
    private boolean hasReviewNotificationBeenSent(Long bookingId, Long userId) {
        // Check if a review notification has already been sent for this booking to this specific user
        try {
            Long count = notificationRepository.findNotificationCountByTypeEntityIdAndRecipient("REVIEW_REQUEST", bookingId, userId);
            boolean alreadySent = count > 0;
            log.info("Review notification check for booking {} and user {}: count={}, alreadySent={}", bookingId, userId, count, alreadySent);
            return alreadySent;
        } catch (Exception e) {
            log.error("Error checking review notification status for booking {} and user {}: ", bookingId, userId, e);
            return false; // If error occurs, allow sending to be safe
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