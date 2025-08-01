package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.TeamRoster;
import fpt.aptech.management_field.models.TeamRosterId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRosterRepository extends JpaRepository<TeamRoster, TeamRosterId> {
}
