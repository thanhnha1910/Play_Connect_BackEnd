package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.Notification;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SchedulingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0/15 * * * ?") // Runs every 15 minutes
    @Transactional
    public void sendBookingReminders() {
        try {
            // Get current time and 2 hours from now
            Instant now = Instant.now();
            Instant twoHoursLater = now.plusSeconds(2 * 60 * 60);

            // Find bookings that start within the next 2 hours and haven't had reminders sent
            List<Booking> upcomingBookings = bookingRepository.findUpcomingBookingsForReminder(now, twoHoursLater);

            for (Booking booking : upcomingBookings) {
                try {
                    // Send email reminder
                    sendEmailReminder(booking);

                    // Create in-app notification
                    createInAppNotification(booking);

                    // Mark reminder as sent
                    booking.setReminderSent(true);
                    bookingRepository.save(booking);

                } catch (Exception e) {
                    System.err.println("Failed to send reminder for booking " + booking.getBookingId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error in sendBookingReminders: " + e.getMessage());
        }
    }

    private void sendEmailReminder(Booking booking) {
        try {
            String userEmail = booking.getUser().getEmail();
            String fieldName = booking.getField().getName();
            LocalDateTime startTime = LocalDateTime.ofInstant(booking.getFromTime(), ZoneId.systemDefault());
            
            String subject = "Nhắc nhở: Lịch đặt sân sắp bắt đầu";
            String content = String.format(
                "Xin chào %s,\n\n" +
                "Lịch đặt sân của bạn tại %s sắp bắt đầu lúc %s.\n\n" +
                "Vui lòng chuẩn bị và đến sân đúng giờ.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ PlayerConnect",
                booking.getUser().getFullName(),
                fieldName,
                startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
            
            emailService.sendEmail(userEmail, subject, content);
        } catch (Exception e) {
            System.err.println("Failed to send email reminder: " + e.getMessage());
        }
    }

    private void createInAppNotification(Booking booking) {
        try {
            String fieldName = booking.getField().getName();
            LocalDateTime startTime = LocalDateTime.ofInstant(booking.getFromTime(), ZoneId.systemDefault());
            
            Notification notification = new Notification();
            notification.setTitle("Lịch đặt sân sắp bắt đầu");
            notification.setContent(String.format(
                "Lịch đặt sân của bạn tại %s sắp bắt đầu lúc %s",
                fieldName,
                startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ));
            notification.setRecipient(booking.getUser());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setIsRead(false);
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("Failed to create in-app notification: " + e.getMessage());
        }
    }
}