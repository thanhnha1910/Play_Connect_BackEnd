package fpt.aptech.management_field.services;

import fpt.aptech.management_field.events.BookingConfirmedEvent;
import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberLevelUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(MemberLevelUpdateService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserService userService;

    @EventListener
    @Transactional
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        try {
            Booking booking = event.getBooking();
            User user = booking.getUser();
            
            logger.info("Processing member level update for user {} after booking {}", 
                       user.getId(), booking.getBookingId());
            
            // Đếm tổng số booking đã confirmed của user
            int confirmedBookingCount = bookingRepository.countByUserIdAndStatus(user.getId(), "confirmed");
            
            // Cập nhật booking count
            user.setBookingCount(confirmedBookingCount);
            
            // Tính toán member level mới dựa trên booking count
            int newMemberLevel = userService.calculateLevel(confirmedBookingCount);
            int oldMemberLevel = user.getMemberLevel() != null ? user.getMemberLevel() : 0;
            
            // Cập nhật member level
            user.setMemberLevel(newMemberLevel);
            
            // Lưu user với thông tin cập nhật
            userRepository.save(user);
            
            logger.info("Updated user {} - Booking count: {}, Member level: {} -> {}", 
                       user.getId(), confirmedBookingCount, oldMemberLevel, newMemberLevel);
            
            // Log level up event
            if (newMemberLevel > oldMemberLevel) {
                logger.info("🎉 User {} leveled up from {} to {}!", 
                           user.getId(), oldMemberLevel, newMemberLevel);
            }
            
        } catch (Exception e) {
            logger.error("Error updating member level after booking confirmation: {}", e.getMessage(), e);
            // Không throw exception để không ảnh hưởng đến booking process
        }
    }
}