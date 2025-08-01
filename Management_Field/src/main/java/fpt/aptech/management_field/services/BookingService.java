package fpt.aptech.management_field.services;

import fpt.aptech.management_field.events.BookingConfirmedEvent;
import fpt.aptech.management_field.mappers.BookingMapper;
import fpt.aptech.management_field.models.*;
import fpt.aptech.management_field.payload.dtos.BookingDTO;
import fpt.aptech.management_field.payload.dtos.BookingHistoryDto;
import fpt.aptech.management_field.payload.request.BookingRequest;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.BookingUserRepository;
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

    public List<BookingDTO> getBookingsByDate(Instant startDate, Instant endDate, Long fieldId) {
        try {
            List<Booking> bookings = bookingRepository.findForFieldByDate(startDate, endDate, fieldId);
            return BookingMapper.listToDTO(bookings);
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
    private PaymentService paymentService;

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

        float totalPrice = field.getHourlyRate() * Duration.between(bookingRequest.getFromTime(), bookingRequest.getToTime()).toHours();
        String payUrl = paymentService.initiatePayPal(booking.getBookingId(), PaymentPayable.BOOKING, (int) totalPrice);

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

        // Calculate total price - fix type conversion
        long hours = java.time.Duration.between(booking.getFromTime(), booking.getToTime()).toHours();
        dto.setTotalPrice((double) (booking.getField().getHourlyRate() * hours));

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
            payPalPaymentService.capturePayment(token);

            // Update booking status
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

        } catch (Exception e) {
            throw new RuntimeException("Payment callback processing failed: " + e.getMessage());
        }
    }

}
