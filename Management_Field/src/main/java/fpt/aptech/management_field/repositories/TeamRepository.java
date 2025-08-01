package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query("SELECT tr.team FROM TeamRoster tr WHERE tr.userId = :userId")
    List<Team> findByUserId(@Param("userId") Long userId);

    Optional<Team> findByName(String name);
}
