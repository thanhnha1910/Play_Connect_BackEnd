package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Role;
import fpt.aptech.management_field.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    Optional<User> findByResetPasswordToken(String token);
    
    Optional<User> findByVerificationToken(String token);
    
    List<User> findByRoles_NameAndStatus(Role.ERole roleName, User.UserStatus status);
    
    Long countByRoles_NameAndStatus(Role.ERole roleName, User.UserStatus status);
    
    Long countByStatus(User.UserStatus status);
}