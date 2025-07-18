package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.payload.dtos.BookingDTO;
import fpt.aptech.management_field.payload.dtos.BookingReceiptDTO;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class BookingMapper {
    public static BookingDTO mapToDTO(Booking booking) {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setFromTime(booking.getFromTime());
        bookingDTO.setToTime(booking.getToTime());
        bookingDTO.setSlots(booking.getSlots());
        bookingDTO.setStatus(booking.getStatus());
        
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

    public static List<BookingDTO> listToDTO(List<Booking> bookings) {
        List<BookingDTO> bookingDTOS = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingDTOS.add(mapToDTO(booking));
        }
        return bookingDTOS;
    }
    
    public static BookingReceiptDTO mapToReceiptDTO(Booking booking) {
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
        
        // Calculate duration and total price
        if (booking.getFromTime() != null && booking.getToTime() != null) {
            long hours = Duration.between(booking.getFromTime(), booking.getToTime()).toHours();
            receiptDTO.setDurationHours(hours);
            
            if (booking.getField() != null && booking.getField().getHourlyRate() != null) {
                receiptDTO.setTotalPrice((double) (booking.getField().getHourlyRate() * hours));
            }
        }
        
        return receiptDTO;
    }
}
