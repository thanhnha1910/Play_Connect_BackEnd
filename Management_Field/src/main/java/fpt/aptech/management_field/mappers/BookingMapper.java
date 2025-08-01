package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.OpenMatch;
import fpt.aptech.management_field.payload.dtos.BookingDTO;
import fpt.aptech.management_field.payload.dtos.BookingReceiptDTO;
import fpt.aptech.management_field.repositories.OpenMatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class BookingMapper {
    
    @Autowired
    private OpenMatchRepository openMatchRepository;
    public BookingDTO mapToDTO(Booking booking , int basePrice, int discountPercent, int discountAmount, int totalPrice) {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setFromTime(booking.getFromTime());
        bookingDTO.setToTime(booking.getToTime());
        bookingDTO.setSlots(booking.getSlots());
        bookingDTO.setStatus(booking.getStatus());
                bookingDTO.setBasePrice(basePrice);
        bookingDTO.setDiscountPercent(discountPercent);
        bookingDTO.setDiscountAmount(discountAmount);
        bookingDTO.setTotalPrice(totalPrice);
        
        // Set customer information if booking has users
        if (booking.getBookingUsers() != null && !booking.getBookingUsers().isEmpty()) {
            var firstUser = booking.getBookingUsers().iterator().next().getUser();
            bookingDTO.setCustomerName(firstUser.getFullName());
            bookingDTO.setCustomerPhone(firstUser.getPhoneNumber());
        }
        
        // Set isBooked based on status (both confirmed and pending are considered booked)
        bookingDTO.setBooked("CONFIRMED".equals(booking.getStatus()) || "PENDING".equals(booking.getStatus()));
        
        return bookingDTO;
    }

    @Autowired
    private fpt.aptech.management_field.services.UserService userService;
    
    public List<BookingDTO> listToDTO(List<Booking> bookings) {
        List<BookingDTO> bookingDTOS = new ArrayList<>();
        for (Booking booking : bookings) {
            // Calculate proper pricing with discount
            long hours = Duration.between(booking.getFromTime(), booking.getToTime()).toHours();
            int basePrice = booking.getField().getHourlyRate() * (int) hours;
            
            // Apply discount if user has memberLevel
            int discountPercent = 0;
            int discountAmount = 0;
            int totalPrice = basePrice;
            
            if (booking.getUser() != null) {
                Integer memberLevel = booking.getUser().getMemberLevel();
                discountPercent = userService.getDiscountPercent(memberLevel != null ? memberLevel : 0);
                discountAmount = basePrice * discountPercent / 100;
                totalPrice = basePrice - discountAmount;
            }
            
            bookingDTOS.add(mapToDTO(booking, basePrice, discountPercent, discountAmount, totalPrice));
        }
        return bookingDTOS;
    }
    
    public BookingReceiptDTO mapToReceiptDTO(Booking booking) {
        BookingReceiptDTO receiptDTO = new BookingReceiptDTO();
        
        // Basic booking information
        receiptDTO.setBookingId(booking.getBookingId());
        receiptDTO.setFromTime(booking.getFromTime());
        receiptDTO.setToTime(booking.getToTime());
        receiptDTO.setSlots(booking.getSlots());
        receiptDTO.setStatus(booking.getStatus());
        receiptDTO.setPaymentToken(booking.getPaymentToken());
        
        // User information
        if (booking.getUser() != null) {
            receiptDTO.setUserId(booking.getUser().getId());
            receiptDTO.setCustomerName(booking.getUser().getFullName());
            receiptDTO.setCustomerEmail(booking.getUser().getEmail());
            receiptDTO.setCustomerPhone(booking.getUser().getPhoneNumber());
        }
        
        // Field information
        if (booking.getField() != null) {
            receiptDTO.setFieldId(booking.getField().getFieldId());
            receiptDTO.setFieldName(booking.getField().getName());
            receiptDTO.setFieldDescription(booking.getField().getDescription());
            receiptDTO.setHourlyRate(booking.getField().getHourlyRate().doubleValue());
            
            // Location information
            if (booking.getField().getLocation() != null) {
                receiptDTO.setLocationName(booking.getField().getLocation().getName());
                receiptDTO.setLocationAddress(booking.getField().getLocation().getAddress());
            }
            
            // Field type information
            if (booking.getField().getType() != null) {
                receiptDTO.setFieldTypeName(booking.getField().getType().getName());
            }
            
            // Field category information
            if (booking.getField().getCategory() != null) {
                receiptDTO.setFieldCategoryName(booking.getField().getCategory().getName());
            }
        }
        
        // Calculate duration and total price with discount
        if (booking.getFromTime() != null && booking.getToTime() != null) {
            long hours = Duration.between(booking.getFromTime(), booking.getToTime()).toHours();
            receiptDTO.setDurationHours(hours);
            
            if (booking.getField() != null && booking.getField().getHourlyRate() != null) {
                double basePrice = booking.getField().getHourlyRate() * hours;
                
                // Apply discount if user has memberLevel
                if (booking.getUser() != null) {
                    Integer memberLevel = booking.getUser().getMemberLevel();
                    int discountPercent = userService.getDiscountPercent(memberLevel != null ? memberLevel : 0);
                    double discountAmount = basePrice * discountPercent / 100;
                    receiptDTO.setTotalPrice(basePrice - discountAmount);
                } else {
                    receiptDTO.setTotalPrice(basePrice);
                }
            }
        }
        
        // Open match information (if exists)
        OpenMatch openMatch = openMatchRepository.findByBookingId(booking.getBookingId());
        if (openMatch != null) {
            BookingReceiptDTO.OpenMatchSummaryDto openMatchDto = new BookingReceiptDTO.OpenMatchSummaryDto();
            openMatchDto.setId(openMatch.getId());
            openMatchDto.setSportType(openMatch.getSportType());
            openMatchDto.setSlotsNeeded(openMatch.getSlotsNeeded());
            openMatchDto.setStatus(openMatch.getStatus());
            receiptDTO.setOpenMatch(openMatchDto);
        }
        
        return receiptDTO;
    }
}
