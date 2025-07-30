package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.ParticipatingTeam;
import fpt.aptech.management_field.models.ParticipatingTeamId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipatingTeamRepository extends JpaRepository<ParticipatingTeam, ParticipatingTeamId> {
    Optional<ParticipatingTeam> findByTeamIdAndTournamentId(Long teamId, Long tournamentId);
    Optional<ParticipatingTeam> findByTeamId(Long teamId);
    List<ParticipatingTeam> findByTournamentId(Long tournamentId);
}