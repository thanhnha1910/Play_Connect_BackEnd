package fpt.aptech.management_field.services;

import fpt.aptech.management_field.mappers.BookingMapper;
import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.payload.dtos.BookingDTO;
import fpt.aptech.management_field.repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    public List<BookingDTO> getBookingsByDate(LocalDateTime startDate, LocalDateTime endDate, Long fieldId) {
        List<Booking> bookings = bookingRepository.findForFieldByDate(startDate, endDate, fieldId);
        return BookingMapper.listToDTO(bookings);
    }
}
