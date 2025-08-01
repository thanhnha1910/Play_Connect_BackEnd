package fpt.aptech.management_field.services;

import fpt.aptech.management_field.events.BookingConfirmedEvent;
import fpt.aptech.management_field.mappers.BookingMapper;
import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.BookingUser;
import fpt.aptech.management_field.models.DraftMatch;
import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.models.FieldClosure;
import fpt.aptech.management_field.models.Notification;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.BookingDTO;
import fpt.aptech.management_field.payload.dtos.BookingHistoryDto;
import fpt.aptech.management_field.payload.request.BookingRequest;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.BookingUserRepository;
import fpt.aptech.management_field.repositories.DraftMatchRepository;
import fpt.aptech.management_field.repositories.FieldRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private BookingMapper bookingMapper;
     @Autowired
     private UserService userService;

    public List<BookingDTO> getBookingsByDate(Instant startDate, Instant endDate, Long fieldId) {
        try {
            List<Booking> bookings = bookingRepository.findForFieldByDate(startDate, endDate, fieldId);
            return bookingMapper.listToDTO(bookings);
        } catch (Exception e) {
            System.out.println("Error fetching bookings: " + e.getMessage());
            // Return empty list as fallback to prevent API failure
            return new ArrayList<>();
        }
    }


    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingUserRepository bookingUserRepository;

    @Autowired
    private PayPalPaymentService payPalPaymentService;
    
    @Autowired
    private DraftMatchRepository draftMatchRepository;
    
    @Autowired
    private NotificationService notificationService;
    @Transactional
    public Map<String, Object> createBooking(Long userId, BookingRequest bookingRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Field field = fieldRepository.findById(bookingRequest.getFieldId())
                .orElseThrow(() -> new RuntimeException("Field not found"));

        // Convert Instant to LocalDateTime for comparison with FieldClosure dates
        LocalDateTime fromTimeLocal = LocalDateTime.ofInstant(bookingRequest.getFromTime(), ZoneId.systemDefault());
        LocalDateTime toTimeLocal = LocalDateTime.ofInstant(bookingRequest.getToTime(), ZoneId.systemDefault());
        
        // Custom validation for booking time (replacing @Future annotation)
        LocalDateTime nowLocal = LocalDateTime.now(ZoneId.systemDefault());
        
        // Allow booking within 24 hours ahead, calculated in local timezone
        if (fromTimeLocal.isBefore(nowLocal.minusHours(1))) {
            throw new RuntimeException("Cannot book for past time slots");
        }
        
        if (toTimeLocal.isBefore(fromTimeLocal)) {
            throw new RuntimeException("End time must be after start time");
        }
        
        // Check if field is active
        if (field.getIsActive() == null || !field.getIsActive()) {
            throw new RuntimeException("Field is not available");
        }
        
        // Check field closures
        List<FieldClosure> fieldClosures = field.getFieldClosures();
        if (fieldClosures != null) {
            for (FieldClosure closure : fieldClosures) {
                if (fromTimeLocal.isBefore(closure.getEndDate()) && toTimeLocal.isAfter(closure.getStartDate())) {
                    throw new RuntimeException("Field is not available");
                }
            }
        }

        if (bookingRepository.existsByFieldAndFromTimeLessThanEqualAndToTimeGreaterThanEqual(
                field, bookingRequest.getToTime(), bookingRequest.getFromTime())) {
            throw new RuntimeException("Field is already booked for this time slot");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setField(field);
        booking.setFromTime(bookingRequest.getFromTime());
        booking.setToTime(bookingRequest.getToTime());
        booking.setSlots(bookingRequest.getSlots());
        booking.setStatus("pending");

        booking = bookingRepository.save(booking);

        if (bookingRequest.isFindTeammates() && bookingRequest.getAdditionalPlayers() != null) {
            for (BookingRequest.AdditionalPlayer player : bookingRequest.getAdditionalPlayers()) {
                User teammate = userRepository.findById(player.getUserId())
                        .orElseThrow(() -> new RuntimeException("Teammate not found"));
                BookingUser bookingUser = new BookingUser();
                bookingUser.setBookingId(booking.getBookingId());
                bookingUser.setUserId(teammate.getId());
                bookingUser.setIsBooker(false);
                bookingUser.setPosition(player.getPosition());
                bookingUser.setBooking(booking);
                bookingUser.setUser(teammate);
                bookingUserRepository.save(bookingUser);
            }
        }

        // float totalPrice = field.getHourlyRate() * Duration.between(bookingRequest.getFromTime(), bookingRequest.getToTime()).toHours();
        // String payUrl = payPalPaymentService.initiatePayPalPayment(booking.getBookingId(), totalPrice);

long hours = Duration.between(bookingRequest.getFromTime(), bookingRequest.getToTime()).toHours();
        float basePrice = field.getHourlyRate() * hours;
        // Thêm null check cho memberLevel
        Integer memberLevel = user.getMemberLevel();
        int discountPercent = userService.getDiscountPercent(memberLevel != null ? memberLevel : 0);
        float discountAmount = basePrice * discountPercent / 100;
        float finalPrice = basePrice - discountAmount;
        String payUrl = payPalPaymentService.initiatePayPalPayment(booking.getBookingId(), finalPrice);


        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", booking.getBookingId());
        response.put("payUrl", payUrl);
        return response;
    }

    @Transactional
    public Booking confirmPayment(Long bookingId, String token, String payerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!"pending".equals(booking.getStatus())) {
            throw new RuntimeException("Booking is not in pending state");
        }
        booking.setPaymentToken(token);
        booking.setStatus("confirmed");
        Booking savedBooking = bookingRepository.save(booking);
        
        // Publish booking confirmed event
        eventPublisher.publishEvent(new BookingConfirmedEvent(this, savedBooking));
        
        return savedBooking;
    }

    public List<Booking> getBookingHistory(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<BookingHistoryDto> getBookingsForUser(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByFromTimeDesc(userId);
        return bookings.stream().map(this::convertToBookingHistoryDto).toList();
    }

    private BookingHistoryDto convertToBookingHistoryDto(Booking booking) {
        BookingHistoryDto dto = new BookingHistoryDto();
        dto.setBookingId(booking.getBookingId());
        dto.setFieldName(booking.getField().getName());
        
        // Get address from the location if available
        if (booking.getField().getLocation() != null) {
            dto.setFieldAddress(booking.getField().getLocation().getAddress());
        } else {
            dto.setFieldAddress("Address not available");
        }
        
        // Set a default cover image or null if not available
        dto.setCoverImageUrl(null); // or set a default image URL
        
        dto.setStartTime(booking.getFromTime());
        dto.setEndTime(booking.getToTime());
        
        // Calculate total price with discount - fix type conversion and add discount calculation
        long hours = java.time.Duration.between(booking.getFromTime(), booking.getToTime()).toHours();
        double basePrice = booking.getField().getHourlyRate() * hours;
        
        // Apply discount if user has memberLevel
        if (booking.getUser() != null) {
            Integer memberLevel = booking.getUser().getMemberLevel();
            int discountPercent = userService.getDiscountPercent(memberLevel != null ? memberLevel : 0);
            double discountAmount = basePrice * discountPercent / 100;
            dto.setTotalPrice(basePrice - discountAmount);
        } else {
            dto.setTotalPrice(basePrice);
        }
        
        dto.setStatus(booking.getStatus());
        return dto;
    }

    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElse(null);
    }

    @Transactional
    public Booking cancelBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized to cancel this booking");
        }
        if (!"pending".equals(booking.getStatus())) {
            throw new RuntimeException("Only pending bookings can be canceled");
        }
        booking.setStatus("canceled");
        bookingRepository.save(booking);
        bookingUserRepository.deleteByBookingId(bookingId);
        return booking;
    }

    @Transactional
    public Booking handlePaymentCallback(String token, String payerId, String bookingIdStr) {
        try {
            // Extract booking ID from token or parameter
            Long bookingId;
            if (bookingIdStr != null) {
                bookingId = Long.valueOf(bookingIdStr);
            } else {
                // Extract from token if bookingId not provided
                // This assumes token contains booking info
                throw new RuntimeException("Booking ID not provided in callback");
            }
            
            // Capture the payment through PayPal
            payPalPaymentService.capturePayment(bookingId, token, payerId);
            
            // Update booking status
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            if (!"pending".equals(booking.getStatus())) {
                throw new RuntimeException("Booking is not in pending state");
            }
            User user = booking.getUser();
            int updatedBookingCount = user.getBookingCount() + 1;
            user.setBookingCount(updatedBookingCount);

            int newLevel = userService.calculateLevel(updatedBookingCount);
            user.setMemberLevel(newLevel);

            booking.setPaymentToken(token);
            booking.setStatus("confirmed");
             userRepository.save(user);

            Booking savedBooking = bookingRepository.save(booking);
            
            long hours = Duration.between(booking.getFromTime(), booking.getToTime()).toHours();
            int basePrice = booking.getField().getHourlyRate() * (int) hours;
            // Thêm null check cho memberLevel
            Integer memberLevel = user.getMemberLevel();
            int discountPercent = userService.getDiscountPercent(memberLevel != null ? memberLevel : 0);
            int discountAmount = basePrice * discountPercent / 100;
            int totalPrice = basePrice - discountAmount;
            // Publish booking confirmed event
            eventPublisher.publishEvent(new BookingConfirmedEvent(this, savedBooking));
            
            return savedBooking;
            
        } catch (Exception e) {
            throw new RuntimeException("Payment callback processing failed: " + e.getMessage());
        }
    }
    
    @Transactional
    public Map<String, Object> convertDraftMatchToBooking(Long draftMatchId, Long bookingId, Long userId) {
        // Find the draft match
        DraftMatch draftMatch = draftMatchRepository.findById(draftMatchId)
                .orElseThrow(() -> new RuntimeException("Draft match not found"));
        
        // Verify the user is the creator
        if (!draftMatch.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can convert this draft match");
        }
        
        // Verify the draft match is in AWAITING_CONFIRMATION status
        if (!"AWAITING_CONFIRMATION".equals(draftMatch.getStatus())) {
            throw new RuntimeException("Draft match is not in awaiting confirmation status");
        }
        
        // Find the booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Verify the booking belongs to the same user
        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("Booking does not belong to the user");
        }
        
        // Update draft match status to CONVERTED_TO_MATCH
        draftMatch.setStatus("CONVERTED_TO_MATCH");
        draftMatch = draftMatchRepository.save(draftMatch);
        
        // Send notifications to all interested users
        for (User interestedUser : draftMatch.getInterestedUsers()) {
            createNotificationForDraftMatchConfirmed(draftMatch, booking, interestedUser);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("draftMatchId", draftMatchId);
        result.put("bookingId", bookingId);
        result.put("status", draftMatch.getStatus());
        result.put("notificationsSent", draftMatch.getInterestedUsers().size());
        
        return result;
    }
    
    private void createNotificationForDraftMatchConfirmed(DraftMatch draftMatch, Booking booking, User recipient) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle("Kèo đã được chốt!");
        notification.setContent(String.format(
            "Kèo đã được chốt! Trận đấu '%s' sẽ diễn ra tại %s lúc %s. Vui lòng xác nhận tham gia lần cuối.",
            draftMatch.getSportType(),
            booking.getField().getName(),
            booking.getFromTime().toString()
        ));
        notification.setType("DRAFT_MATCH_CONFIRMED");
        notification.setRelatedEntityId(booking.getBookingId());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }

}
