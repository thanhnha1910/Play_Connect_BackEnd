package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.ParticipatingTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipatingTeamRepository extends JpaRepository<ParticipatingTeam, Long> {
    @Query("SELECT pt FROM ParticipatingTeam pt WHERE pt.team.teamId = :teamId AND pt.tournament.tournamentId = :tournamentId")
    Optional<ParticipatingTeam> findByTeamIdAndTournamentId(Long teamId, Long tournamentId);

    @Query("SELECT pt FROM ParticipatingTeam pt WHERE pt.team.teamId = :teamId")
    Optional<ParticipatingTeam> findByTeamId(Long teamId);

    @Query("SELECT pt FROM ParticipatingTeam pt WHERE pt.tournament.tournamentId = :tournamentId")
    List<ParticipatingTeam> findByTournamentId(Long tournamentId);
}