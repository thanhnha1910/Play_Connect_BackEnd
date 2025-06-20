package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.BookingUser;
import fpt.aptech.management_field.models.BookingUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingUserRepository extends JpaRepository<BookingUser, BookingUserId> {

    @Modifying
    @Query("DELETE FROM BookingUser bu WHERE bu.bookingId = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);
}