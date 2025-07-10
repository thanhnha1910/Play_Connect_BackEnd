package fpt.aptech.management_field.services;

import fpt.aptech.management_field.mappers.BookingMapper;
import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.BookingUser;
import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.models.FieldClosure;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.BookingDTO;
import fpt.aptech.management_field.payload.request.BookingRequest;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.BookingUserRepository;
import fpt.aptech.management_field.repositories.FieldRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
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
    private UserService userService;

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

        long hours = Duration.between(bookingRequest.getFromTime(), bookingRequest.getToTime()).toHours();
        int basePrice = field.getHourlyRate() * (int) hours;

        int discountPercent = userService.getDiscountPercent(user.getMemberLevel());
        int discountAmount = basePrice * discountPercent / 100;

        float totalPrice = basePrice - discountAmount;

        String payUrl = payPalPaymentService.initiatePayPalPayment(booking.getBookingId(), totalPrice);

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

        // ✅ Cập nhật thông tin user
        User user = booking.getUser();
        int updatedBookingCount = user.getBookingCount() + 1;
        user.setBookingCount(updatedBookingCount);

        int newLevel = userService.calculateLevel(updatedBookingCount);
        user.setMemberLevel(newLevel);

        userRepository.save(user);

        return bookingRepository.save(booking);

    }

    public List<Booking> getBookingHistory(Long userId) {
        return bookingRepository.findByUserId(userId);
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
    public Map<String, Object> handlePaymentCallback(String token, String payerId, String bookingIdStr) {
        try {
            Long bookingId = (bookingIdStr != null) ? Long.valueOf(bookingIdStr) : null;
            if (bookingId == null) {
                throw new RuntimeException("Booking ID is missing");
            }

            // Capture the payment
            payPalPaymentService.capturePayment(bookingId, token, payerId);

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            if (!"pending".equals(booking.getStatus())) {
                throw new RuntimeException("Booking is not in pending state");
            }

            booking.setPaymentToken(token);
            booking.setStatus("confirmed");

            // Cập nhật lại người dùng
            User user = booking.getUser();
            int updatedBookingCount = user.getBookingCount() + 1;
            user.setBookingCount(updatedBookingCount);

            int newLevel = userService.calculateLevel(updatedBookingCount);
            user.setMemberLevel(newLevel);

            userRepository.save(user);
            bookingRepository.save(booking);

            // Tính giá
            long hours = Duration.between(booking.getFromTime(), booking.getToTime()).toHours();
            int basePrice = booking.getField().getHourlyRate() * (int) hours;
            int discountPercent = userService.getDiscountPercent(user.getMemberLevel());
            int discountAmount = basePrice * discountPercent / 100;
            int totalPrice = basePrice - discountAmount;
            BookingDTO dto = BookingMapper.mapToDTO(booking, basePrice, discountPercent, discountAmount, totalPrice);
            // Trả về thông tin đầy đủ
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Payment completed successfully");
            response.put("booking", dto);
            response.put("basePrice", basePrice);
            response.put("discountPercent", discountPercent);
            response.put("discountAmount", discountAmount);
            response.put("totalPrice", totalPrice);
            System.out.println(">>> Booking ID: " + bookingId);
            System.out.println(">>> User Level: " + user.getMemberLevel());
            System.out.println(">>> Base Price: " + basePrice);
            System.out.println(">>> Discount: " + discountAmount);
            System.out.println(">>> Total Price: " + totalPrice);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Payment callback processing failed: " + e.getMessage());
        }
    }





}
