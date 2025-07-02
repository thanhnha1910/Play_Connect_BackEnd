package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Owner;
import fpt.aptech.management_field.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    Optional<Owner> findByUser(User user);
    Optional<Owner> findByUserId(Long userId);
    boolean existsByUser(User user);
}