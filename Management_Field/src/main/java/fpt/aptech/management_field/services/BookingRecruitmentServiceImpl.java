package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.BookingRequirement;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.repositories.BookingRecruitmentRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingRecruitmentServiceImpl implements BookingRecruitmentService {

    private final BookingRecruitmentRepository bookingRecruitmentRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingRecruitmentServiceImpl(BookingRecruitmentRepository bookingRecruitmentRepository,
                                         UserRepository userRepository) {
        this.bookingRecruitmentRepository = bookingRecruitmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public BookingRequirement create(BookingRequirement bookingRecruitment) {
        return bookingRecruitmentRepository.save(bookingRecruitment);
    }

    @Override
    public BookingRequirement createFromUser(Long userId, Integer peopleNeeded, String message) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        User user = optionalUser.get();

        BookingRequirement recruitment = new BookingRequirement();
        recruitment.setUser(user); // ✅ This line is required

        recruitment.setUserName(user.getFullName() != null ? user.getFullName() : "Anonymous");
        recruitment.setPhone(user.getPhoneNumber() != null ? user.getPhoneNumber() : "No phone");
        recruitment.setFieldNumber("Unknown Field"); // Default, replace later if needed
        recruitment.setFieldLocation("Unknown Location"); // Default, replace later if needed
        recruitment.setPlayTime(LocalDateTime.now().plusDays(1)); // Default to tomorrow
        recruitment.setPeopleNeeded(peopleNeeded);
        recruitment.setMessage(message != null ? message : "Looking for players");
        recruitment.setCreatedAt(LocalDateTime.now());

        return bookingRecruitmentRepository.save(recruitment);
    }

    @Override
    public List<BookingRequirement> findAll() {
        return (List<BookingRequirement>) bookingRecruitmentRepository.findAll();
    }

    @Override
    public List<BookingRequirement> findByUserId(Long userId) {
        return bookingRecruitmentRepository.findByUserId(userId);
    }

    @Override
    public void deleteById(Long id) {
        bookingRecruitmentRepository.deleteById(id);
    }
}
