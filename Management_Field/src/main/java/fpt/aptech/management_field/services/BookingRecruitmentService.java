package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.BookingRequirement;

import java.util.List;

public interface BookingRecruitmentService {
    BookingRequirement create(BookingRequirement bookingRecruitment);
    BookingRequirement createFromUser(Long userId, Integer peopleNeeded, String message);
    List<BookingRequirement> findAll();
    void deleteById(Long id);
    List<BookingRequirement> findByUserId(Long userId);
}
