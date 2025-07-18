package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.OpenMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpenMatchRepository extends JpaRepository<OpenMatch, Long> {
    
    @Query("SELECT om FROM OpenMatch om WHERE om.status = 'OPEN' AND om.sportType = :sportType")
    List<OpenMatch> findOpenMatchesBySportType(@Param("sportType") String sportType);
    
    @Query("SELECT om FROM OpenMatch om WHERE om.status = 'OPEN'")
    List<OpenMatch> findAllOpenMatches();
    
    @Query("SELECT om FROM OpenMatch om WHERE om.creatorUser.id = :userId")
    List<OpenMatch> findByCreatorUserId(@Param("userId") Long userId);
    
    @Query("SELECT om FROM OpenMatch om WHERE om.booking.bookingId = :bookingId")
    OpenMatch findByBookingId(@Param("bookingId") Long bookingId);
}