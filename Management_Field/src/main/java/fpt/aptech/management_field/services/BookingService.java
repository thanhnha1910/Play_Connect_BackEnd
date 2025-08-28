package fpt.aptech.management_field.services;

import fpt.aptech.management_field.events.BookingConfirmedEvent;
import fpt.aptech.management_field.mappers.BookingMapper;
import fpt.aptech.management_field.models.*;
import fpt.aptech.management_field.models.PaymentPayable;
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
import fpt.aptech.management_field.payload.request.BatchBookingRequest;

import fpt.aptech.management_field.repositories.AdminRevenueRepository;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.BookingUserRepository;
import fpt.aptech.management_field.repositories.DraftMatchRepository;
import fpt.aptech.management_field.repositories.FieldRepository;
import fpt.aptech.management_field.repositories.PaymentRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            System.out.println("=== BOOKING QUERY DEBUG ===");
            System.out.println("Field ID: " + fieldId);
            System.out.println("Start Date: " + startDate);
            System.out.println("End Date: " + endDate);
            
            List<Booking> bookings = bookingRepository.findForFieldByDate(startDate, endDate, fieldId);
            System.out.println("Found " + bookings.size() + " bookings");
            
            for (Booking booking : bookings) {
                System.out.println("Booking ID: " + booking.getBookingId() + ", Status: " + booking.getStatus() + ", From: " + booking.getFromTime() + ", To: " + booking.getToTime());
            }
            
            List<BookingDTO> result = bookingMapper.listToDTO(bookings);
            System.out.println("Mapped to " + result.size() + " DTOs");
            return result;
        } catch (Exception e) {
            System.out.println("Error fetching bookings: " + e.getMessage());
            e.printStackTrace();
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

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AdminRevenueRepository adminRevenueRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized Map<String, Object> createBooking(Long userId, BookingRequest bookingRequest, String clientType) {
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

        // // Kiểm tra conflict với lock
        // if (bookingRepository.existsByFieldAndFromTimeLessThanEqualAndToTimeGreaterThanEqual(
        //         field, bookingRequest.getToTime(), bookingRequest.getFromTime())) {
        //     throw new RuntimeException("Field is already booked for this time slot");
        // }
        
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
String payUrl = paymentService.initiatePayPal(booking.getBookingId(), PaymentPayable.BOOKING, (int) finalPrice, clientType);


        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", booking.getBookingId());
        response.put("payUrl", payUrl);
        return response;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized Map<String, Object> createBatchBooking(Long userId, BatchBookingRequest batchRequest, String clientType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Booking> createdBookings = new ArrayList<>();
        float totalAmount = 0;
        
        // Create all bookings first
        for (BookingRequest bookingRequest : batchRequest.getBookingRequests()) {
            Field field = fieldRepository.findById(bookingRequest.getFieldId())
                    .orElseThrow(() -> new RuntimeException("Field not found"));
            
            // Validate booking time and field availability (same as single booking)
            LocalDateTime fromTimeLocal = LocalDateTime.ofInstant(bookingRequest.getFromTime(), ZoneId.systemDefault());
            LocalDateTime toTimeLocal = LocalDateTime.ofInstant(bookingRequest.getToTime(), ZoneId.systemDefault());
            LocalDateTime nowLocal = LocalDateTime.now(ZoneId.systemDefault());
            
            if (fromTimeLocal.isBefore(nowLocal.minusHours(1))) {
                throw new RuntimeException("Cannot book for past time slots");
            }
            
            if (toTimeLocal.isBefore(fromTimeLocal)) {
                throw new RuntimeException("End time must be after start time");
            }
            
            if (field.getIsActive() == null || !field.getIsActive()) {
                throw new RuntimeException("Field is not available");
            }
            
            // Check field closures
            List<FieldClosure> fieldClosures = field.getFieldClosures();
            if (fieldClosures != null) {
                for (FieldClosure closure : fieldClosures) {
                    if (fromTimeLocal.isBefore(closure.getEndDate()) && toTimeLocal.isAfter(closure.getStartDate())) {
                        throw new RuntimeException("Field is not available during closure period");
                    }
                }
            }
            

            
            // Check for overlapping bookings
            if (bookingRepository.existsByFieldAndFromTimeLessThanEqualAndToTimeGreaterThanEqual(
                    field, bookingRequest.getToTime(), bookingRequest.getFromTime())) {
                throw new RuntimeException("Field is already booked for this time slot");
            }
            
            // Create booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setField(field);
            booking.setFromTime(bookingRequest.getFromTime());
            booking.setToTime(bookingRequest.getToTime());
            booking.setSlots(1);
            booking.setStatus("pending");
            booking.setCreatedAt(LocalDateTime.now());
            
            booking = bookingRepository.save(booking);
            createdBookings.add(booking);
            
            // Calculate price for this booking
            long hours = Duration.between(bookingRequest.getFromTime(), bookingRequest.getToTime()).toHours();
            float basePrice = field.getHourlyRate() * hours;
            Integer memberLevel = user.getMemberLevel();
            int discountPercent = userService.getDiscountPercent(memberLevel != null ? memberLevel : 0);
            float discountAmount = basePrice * discountPercent / 100;
            float finalPrice = basePrice - discountAmount;
            totalAmount += finalPrice;
            
            // Create booking user entry for the main user
            BookingUser bookingUser = new BookingUser();
            bookingUser.setBookingId(booking.getBookingId());
            bookingUser.setUserId(user.getId());
            bookingUser.setIsBooker(true);
            bookingUser.setPosition("Goalkeeper"); // Default position
            bookingUser.setBooking(booking);
            bookingUser.setUser(user);
            bookingUserRepository.save(bookingUser);
        }
        
        // Create a single payment for all bookings
        // Use the first booking ID as the primary booking for payment tracking
        Long primaryBookingId = createdBookings.get(0).getBookingId();
        String payUrl = paymentService.initiatePayPal(primaryBookingId, PaymentPayable.BOOKING, (int) totalAmount, clientType);
        
        // Store all booking IDs for later confirmation
        List<Long> bookingIds = createdBookings.stream()
                .map(Booking::getBookingId)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("primaryBookingId", primaryBookingId);
        response.put("bookingIds", bookingIds);
        response.put("totalAmount", totalAmount);
        response.put("payUrl", payUrl);
        return response;
    }

    @Transactional
    public List<Booking> confirmBatchPayment(Long primaryBookingId) {
        System.out.println("=== CONFIRM BATCH PAYMENT DEBUG START ===");
        System.out.println("Primary Booking ID: " + primaryBookingId);
        
        // Find all bookings that were created in the same batch
        Booking primaryBooking = bookingRepository.findById(primaryBookingId)
                .orElseThrow(() -> new RuntimeException("Primary booking not found"));
        
        System.out.println("Primary booking found - User: " + primaryBooking.getUser().getFullName());
        
        // Find all pending bookings for the same user
        List<Booking> userBookings = bookingRepository.findByUserId(primaryBooking.getUser().getId());
        
        // Filter for pending bookings created around the same time (within 5 minutes)
        LocalDateTime cutoffTime = primaryBooking.getCreatedAt().minusMinutes(5);
        List<Booking> batchBookings = userBookings.stream()
                .filter(booking -> "pending".equals(booking.getStatus()))
                .filter(booking -> booking.getCreatedAt().isAfter(cutoffTime))
                .collect(Collectors.toList());
        
        System.out.println("Found " + batchBookings.size() + " bookings in batch");
        
        // Get the primary payment to calculate individual booking amounts
        List<Payment> primaryPayments = paymentRepository.findByPayableIdAndPayableType(primaryBookingId, PaymentPayable.BOOKING);
        Payment primaryPayment = primaryPayments.isEmpty() ? null : primaryPayments.get(0);
        
        // Confirm all bookings in the batch
        List<Booking> confirmedBookings = new ArrayList<>();
        for (int i = 0; i < batchBookings.size(); i++) {
            Booking booking = batchBookings.get(i);
            booking.setStatus("confirmed");
            Booking savedBooking = bookingRepository.save(booking);
            confirmedBookings.add(savedBooking);
            
            System.out.println("Confirmed booking ID: " + savedBooking.getBookingId() + " for field: " + savedBooking.getField().getName());
            
            // Create individual payment record for each booking (except primary which already has one)
            if (!booking.getBookingId().equals(primaryBookingId) && primaryPayment != null) {
                // Calculate individual booking amount
                long hours = Duration.between(booking.getFromTime(), booking.getToTime()).toHours();
                float basePrice = booking.getField().getHourlyRate() * hours;
                Integer memberLevel = booking.getUser().getMemberLevel();
                int discountPercent = userService.getDiscountPercent(memberLevel != null ? memberLevel : 0);
                float discountAmount = basePrice * discountPercent / 100;
                int individualAmount = (int) (basePrice - discountAmount);
                
                // Create payment record for this booking
                Payment individualPayment = new Payment();
                individualPayment.setTotal(individualAmount);
                individualPayment.setMethod(primaryPayment.getMethod());
                individualPayment.setStatus(PaymentStatus.SUCCESS);
                individualPayment.setCreatedAt(LocalDateTime.now());
                individualPayment.setUpdatedAt(LocalDateTime.now());
                individualPayment.setTransactionId(primaryPayment.getTransactionId() + "_" + booking.getBookingId());
                individualPayment.setPayableId(booking.getBookingId());
                individualPayment.setPayableType(PaymentPayable.BOOKING);
                individualPayment.calculateCommission();
                
                paymentRepository.save(individualPayment);
                
                // Create admin revenue record for this payment
                try {
                    AdminRevenue revenue = new AdminRevenue();
                    revenue.setPaymentId(individualPayment.getPaymentId());
                    revenue.setBookingId(booking.getBookingId());
                    revenue.setFieldId(booking.getField().getFieldId());
                    revenue.setOwnerId(booking.getField().getLocation().getOwner().getOwnerId());
                    revenue.setCommissionAmount(individualPayment.getAdminCommission());
                    revenue.setBookingAmount(individualPayment.getTotal());
                    revenue.setOwnerAmount(individualPayment.getOwnerAmount());
                    revenue.setCommissionRate(individualPayment.getCommissionRate());
                    revenue.setCreatedAt(LocalDateTime.now());
                    revenue.setBookingDate(LocalDateTime.ofInstant(booking.getFromTime(), java.time.ZoneId.systemDefault()));
                    revenue.setFieldName(booking.getField().getName());
                    revenue.setLocationName(booking.getField().getLocation().getName());
                    
                    adminRevenueRepository.save(revenue);
                    System.out.println("Created payment and admin revenue record for booking: " + booking.getBookingId());
                } catch (Exception e) {
                    System.err.println("Failed to create admin revenue record for booking " + booking.getBookingId() + ": " + e.getMessage());
                }
            }
            
            // Create notification for field owner
            try {
                User fieldOwner = savedBooking.getField().getLocation().getOwner().getUser();
                System.out.println("[confirmBatchPayment] Field owner: " + fieldOwner.getFullName());
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                String bookingTime = savedBooking.getFromTime().atZone(ZoneId.systemDefault()).format(formatter) + 
                                   " - " + savedBooking.getToTime().atZone(ZoneId.systemDefault()).format(formatter);
                System.out.println("[confirmBatchPayment] Booking time: " + bookingTime);
                
                notificationService.createBookingNotificationForOwner(
                    fieldOwner,
                    savedBooking.getField().getName(),
                    savedBooking.getUser().getFullName(),
                    bookingTime,
                    savedBooking.getBookingId()
                );
                System.out.println("[confirmBatchPayment] Notification created successfully for booking: " + savedBooking.getBookingId());
            } catch (Exception e) {
                System.err.println("[confirmBatchPayment] Failed to send booking notification to owner for booking " + savedBooking.getBookingId() + ": " + e.getMessage());
                e.printStackTrace();
            }
            
            // Update user booking count and member level for the first booking only
            if (booking.getBookingId().equals(primaryBookingId)) {
                User user = booking.getUser();
                int updatedBookingCount = user.getBookingCount() + batchBookings.size();
                user.setBookingCount(updatedBookingCount);
                int newLevel = userService.calculateLevel(updatedBookingCount);
                user.setMemberLevel(newLevel);
                userRepository.save(user);
                System.out.println("Updated user booking count to: " + updatedBookingCount + ", level: " + newLevel);
            }
            
            // Publish booking confirmed event
            BookingConfirmedEvent event = new BookingConfirmedEvent(this, savedBooking);
            eventPublisher.publishEvent(event);
        }
        
        System.out.println("=== CONFIRM BATCH PAYMENT DEBUG END ===");
        return confirmedBookings;
    }

    @Transactional
    public Booking confirmPayment(Long bookingId, String token, String payerId) {
        System.out.println("=== CONFIRM PAYMENT DEBUG START ===");
        System.out.println("Booking ID: " + bookingId);
        System.out.println("Token: " + token);
        System.out.println("Payer ID: " + payerId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!"pending".equals(booking.getStatus())) {
            throw new RuntimeException("Booking is not in pending state");
        }
        
        System.out.println("Booking found - Field: " + booking.getField().getName());
        System.out.println("Booking user: " + booking.getUser().getFullName());
        
        // Update user booking count and member level
        User user = booking.getUser();
        int updatedBookingCount = user.getBookingCount() + 1;
        user.setBookingCount(updatedBookingCount);
        
        int newLevel = userService.calculateLevel(updatedBookingCount);
        user.setMemberLevel(newLevel);
        
        booking.setPaymentToken(token);
        booking.setStatus("confirmed");
        
        // Save user first to update booking count and level
        userRepository.save(user);
        Booking savedBooking = bookingRepository.save(booking);
        System.out.println("Booking saved with status: " + savedBooking.getStatus());

        // Send notification to field owner
        try {
            User fieldOwner = savedBooking.getField().getLocation().getOwner().getUser();
            System.out.println("Field owner found: " + (fieldOwner != null ? fieldOwner.getFullName() : "null"));
            if (fieldOwner != null) {
                LocalDateTime fromTime = LocalDateTime.ofInstant(savedBooking.getFromTime(), ZoneId.systemDefault());
                LocalDateTime toTime = LocalDateTime.ofInstant(savedBooking.getToTime(), ZoneId.systemDefault());
                String bookingTime = fromTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + 
                                   " - " + toTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                
                System.out.println("Creating notification for owner: " + fieldOwner.getFullName());
                System.out.println("Field name: " + savedBooking.getField().getName());
                System.out.println("Customer name: " + savedBooking.getUser().getFullName());
                System.out.println("Booking time: " + bookingTime);
                
                notificationService.createBookingNotificationForOwner(
                    fieldOwner,
                    savedBooking.getField().getName(),
                    savedBooking.getUser().getFullName(),
                    bookingTime,
                    savedBooking.getBookingId()
                );
                System.out.println("Notification created successfully");
            }
        } catch (Exception e) {
            System.err.println("Failed to send booking notification to owner: " + e.getMessage());
            e.printStackTrace();
        }

        // Publish booking confirmed event
        eventPublisher.publishEvent(new BookingConfirmedEvent(this, savedBooking));
        System.out.println("=== CONFIRM PAYMENT DEBUG END ===");

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
        System.out.println("=== HANDLE PAYMENT CALLBACK DEBUG START ===");
        System.out.println("Token: " + token);
        System.out.println("Payer ID: " + payerId);
        System.out.println("Booking ID String: " + bookingIdStr);
        
        try {
            // Extract booking ID from token or parameter
            Long bookingId;
            if (bookingIdStr != null) {
                bookingId = Long.valueOf(bookingIdStr);
                System.out.println("Booking ID extracted: " + bookingId);
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
            User user = booking.getUser();
            int updatedBookingCount = user.getBookingCount() + 1;
            user.setBookingCount(updatedBookingCount);

            int newLevel = userService.calculateLevel(updatedBookingCount);
            user.setMemberLevel(newLevel);

            booking.setPaymentToken(token);
            booking.setStatus("confirmed");
             userRepository.save(user);

            Booking savedBooking = bookingRepository.save(booking);

            // Send notification to field owner
            try {
                User fieldOwner = savedBooking.getField().getLocation().getOwner().getUser();
                System.out.println("[handlePaymentCallback] Field owner found: " + (fieldOwner != null ? fieldOwner.getFullName() : "null"));
                if (fieldOwner != null) {
                    LocalDateTime fromTime = LocalDateTime.ofInstant(savedBooking.getFromTime(), ZoneId.systemDefault());
                    LocalDateTime toTime = LocalDateTime.ofInstant(savedBooking.getToTime(), ZoneId.systemDefault());
                    String bookingTime = fromTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + 
                                       " - " + toTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                    
                    System.out.println("[handlePaymentCallback] Creating notification for owner: " + fieldOwner.getFullName());
                    System.out.println("[handlePaymentCallback] Field name: " + savedBooking.getField().getName());
                    System.out.println("[handlePaymentCallback] Customer name: " + savedBooking.getUser().getFullName());
                    System.out.println("[handlePaymentCallback] Booking time: " + bookingTime);
                    
                    notificationService.createBookingNotificationForOwner(
                        fieldOwner,
                        savedBooking.getField().getName(),
                        savedBooking.getUser().getFullName(),
                        bookingTime,
                        savedBooking.getBookingId()
                    );
                    System.out.println("[handlePaymentCallback] Notification created successfully");
                }
            } catch (Exception e) {
                System.err.println("[handlePaymentCallback] Failed to send booking notification to owner: " + e.getMessage());
                e.printStackTrace();
            }

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

        // Update draft match status to CONVERTED
        draftMatch.setStatus(DraftMatchStatus.CONVERTED);
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
    
    /**
     * Get owner booking statistics with optional facility filter
     */
    public Map<String, Object> getOwnerBookingStats(Long ownerId, Long facilityId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Get statistics with facility filter
        long totalBookings = bookingRepository.countByOwnerAndFacility(ownerId, facilityId);
        long upcomingCount = bookingRepository.countUpcomingByOwnerAndFacility(ownerId, Instant.now(), facilityId);
        long pendingCount = bookingRepository.countPendingByOwnerAndFacility(ownerId, facilityId);
        
        // Calculate this month's revenue with facility filter
        LocalDateTime now = LocalDateTime.now();
        double thisMonthRevenue = bookingRepository.calculateMonthlyRevenueByFacility(
            ownerId, now.getYear(), now.getMonthValue(), facilityId);
        
        stats.put("totalBookings", totalBookings);
        stats.put("upcomingCount", upcomingCount);
        stats.put("pendingCount", pendingCount);
        stats.put("thisMonthRevenue", thisMonthRevenue);
        
        return stats;
    }
    
    // Keep the old method for backward compatibility
    public Map<String, Object> getOwnerBookingStats(Long ownerId) {
        return getOwnerBookingStats(ownerId, null);
    }
    
    /**
     * Update booking status by owner
     */
    @Transactional
    public Booking updateBookingStatus(Long bookingId, String status, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
            
        // Verify that this booking belongs to the owner's field
        if (!booking.getField().getLocation().getOwner().getUser().getId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized: This booking does not belong to your facility");
        }
        
        booking.setStatus(status);
        
        return bookingRepository.save(booking);
    }

    public List<Booking> getRelatedBatchBookings(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return List.of();
        }
        
        // Find all bookings by the same user created within 1 minute of this booking
        // This is more precise for batch bookings created together
        LocalDateTime createdTime = booking.getCreatedAt();
        LocalDateTime startWindow = createdTime.minusMinutes(1); // 1 minute before
        LocalDateTime endWindow = createdTime.plusMinutes(1);   // 1 minute after
        
        List<Booking> relatedBookings = bookingRepository.findByUserIdAndCreatedAtBetween(
            booking.getUser().getId(), startWindow, endWindow);
        
        // Filter to only confirmed bookings and sort by creation time
        return relatedBookings.stream()
            .filter(b -> "confirmed".equals(b.getStatus()))
            .sorted((b1, b2) -> b1.getCreatedAt().compareTo(b2.getCreatedAt()))
            .collect(Collectors.toList());
    }

}