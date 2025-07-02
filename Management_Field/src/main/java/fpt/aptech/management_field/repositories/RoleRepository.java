package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.ERole;
import fpt.aptech.management_field.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}