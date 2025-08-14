package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Tournament;
import fpt.aptech.management_field.payload.dtos.TournamentDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    @Query("SELECT new fpt.aptech.management_field.payload.dtos.TournamentDto(t.tournamentId, t.name, t.slug, t.description, t.startDate, t.endDate, t.prize, t.entryFee, t.slots, t.status, t.location) FROM Tournament t WHERE t.slug = :slug")
    TournamentDto findBySlug(String slug);
    
    // Find tournaments by name (fuzzy matching)chatb
    @Query("SELECT new fpt.aptech.management_field.payload.dtos.TournamentDto(t.tournamentId, t.name, t.slug, t.description, t.startDate, t.endDate, t.prize, t.entryFee, t.slots, t.status, t.location) FROM Tournament t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<TournamentDto> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Find tournaments by location
    @Query("SELECT new fpt.aptech.management_field.payload.dtos.TournamentDto(t.tournamentId, t.name, t.slug, t.description, t.startDate, t.endDate, t.prize, t.entryFee, t.slots, t.status, t.location) FROM Tournament t WHERE LOWER(t.location.name) LIKE LOWER(CONCAT('%', :locationName, '%')) OR LOWER(t.location.address) LIKE LOWER(CONCAT('%', :locationName, '%'))")
    List<TournamentDto> findByLocationNameContainingIgnoreCase(@Param("locationName") String locationName);
    
    // Find tournaments by entry fee range
    @Query("SELECT new fpt.aptech.management_field.payload.dtos.TournamentDto(t.tournamentId, t.name, t.slug, t.description, t.startDate, t.endDate, t.prize, t.entryFee, t.slots, t.status, t.location) FROM Tournament t WHERE t.entryFee BETWEEN :minFee AND :maxFee")
    List<TournamentDto> findByEntryFeeBetween(@Param("minFee") Integer minFee, @Param("maxFee") Integer maxFee);
    
    // Find tournaments by date range
    @Query("SELECT new fpt.aptech.management_field.payload.dtos.TournamentDto(t.tournamentId, t.name, t.slug, t.description, t.startDate, t.endDate, t.prize, t.entryFee, t.slots, t.status, t.location) FROM Tournament t WHERE t.startDate >= :startDate AND t.endDate <= :endDate")
    List<TournamentDto> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find upcoming tournaments
    @Query("SELECT new fpt.aptech.management_field.payload.dtos.TournamentDto(t.tournamentId, t.name, t.slug, t.description, t.startDate, t.endDate, t.prize, t.entryFee, t.slots, t.status, t.location) FROM Tournament t WHERE t.startDate > :currentDate AND (t.status = 'ACTIVE' OR t.status = 'UPCOMING') ORDER BY t.startDate ASC")
    List<TournamentDto> findUpcomingTournaments(@Param("currentDate") LocalDateTime currentDate);
    
    // Find tournaments by status
    @Query("SELECT new fpt.aptech.management_field.payload.dtos.TournamentDto(t.tournamentId, t.name, t.slug, t.description, t.startDate, t.endDate, t.prize, t.entryFee, t.slots, t.status, t.location) FROM Tournament t WHERE t.status = :status ORDER BY t.startDate ASC")
    List<TournamentDto> findByStatus(@Param("status") String status);
    
    // Find all tournaments ordered by start date
    @Query("SELECT new fpt.aptech.management_field.payload.dtos.TournamentDto(t.tournamentId, t.name, t.slug, t.description, t.startDate, t.endDate, t.prize, t.entryFee, t.slots, t.status, t.location) FROM Tournament t ORDER BY t.startDate ASC")
    List<TournamentDto> findAllOrderByStartDate();
}
