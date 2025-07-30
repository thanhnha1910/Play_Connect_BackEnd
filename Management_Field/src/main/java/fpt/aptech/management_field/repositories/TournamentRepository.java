package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Tournament;
import fpt.aptech.management_field.payload.dtos.TournamentDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    @Query("SELECT new fpt.aptech.management_field.payload.dtos.TournamentDto(t.tournamentId, t.name, t.slug, t.description, t.startDate, t.endDate, t.prize, t.entryFee, t.slots, t.status, t.location) FROM Tournament t WHERE t.slug = :slug")
    TournamentDto findBySlug(String slug);
}
