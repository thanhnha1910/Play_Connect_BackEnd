package fpt.aptech.management_field.controllers;


import fpt.aptech.management_field.payload.dtos.BookingDTO;
import fpt.aptech.management_field.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/{fieldId}")
    public ResponseEntity<List<BookingDTO>> getBookingsForFieldByDate(@PathVariable("fieldId") Long fieldId, @RequestParam LocalDateTime fromDate, @RequestParam LocalDateTime toDate) {
        List<BookingDTO> bookingDTOS = bookingService.getBookingsByDate(fromDate, toDate, fieldId);
        return ResponseEntity.ok(bookingDTOS);
    }
}
