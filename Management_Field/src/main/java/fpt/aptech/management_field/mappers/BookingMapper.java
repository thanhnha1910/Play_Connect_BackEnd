package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.payload.dtos.BookingDTO;

import java.util.ArrayList;
import java.util.List;

public class BookingMapper {

    public static BookingDTO mapToDTO(Booking booking, int basePrice, int discountPercent, int discountAmount, int totalPrice) {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setFromTime(booking.getFromTime());
        bookingDTO.setToTime(booking.getToTime());
        bookingDTO.setSlots(booking.getSlots());
        bookingDTO.setStatus(booking.getStatus());
        bookingDTO.setBasePrice(basePrice);
        bookingDTO.setDiscountPercent(discountPercent);
        bookingDTO.setDiscountAmount(discountAmount);
        bookingDTO.setTotalPrice(totalPrice);

        if (booking.getBookingUsers() != null && !booking.getBookingUsers().isEmpty()) {
            var firstUser = booking.getBookingUsers().iterator().next().getUser();
            bookingDTO.setCustomerName(firstUser.getFullName());
            bookingDTO.setCustomerPhone(firstUser.getPhoneNumber());
        }

        bookingDTO.setBooked("CONFIRMED".equalsIgnoreCase(booking.getStatus()) || "PENDING".equalsIgnoreCase(booking.getStatus()));

        return bookingDTO;
    }

    // ✅ Overload đơn giản
    public static BookingDTO mapToDTO(Booking booking) {
        return mapToDTO(booking, 0, 0, 0, 0); // dùng giá trị mặc định
    }

    public static List<BookingDTO> listToDTO(List<Booking> bookings) {
        List<BookingDTO> bookingDTOS = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingDTOS.add(mapToDTO(booking));  // ✅ gọi overload đơn giản
        }
        return bookingDTOS;
    }
}

