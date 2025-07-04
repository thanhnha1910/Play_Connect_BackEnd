package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.payload.dtos.BookingDTO;

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
}
