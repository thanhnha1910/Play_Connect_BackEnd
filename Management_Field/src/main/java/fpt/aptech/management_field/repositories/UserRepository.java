package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.ERole;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.models.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    List<User> findByRoles_NameAndStatus(ERole roleName, UserStatus status);
    
    Long countByRoles_NameAndStatus(ERole roleName, UserStatus status);
    
    Long countByStatus(UserStatus status);
    
    List<User> findByIsDiscoverableTrue();
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.isDiscoverable = true AND r.name = 'ROLE_USER' AND u.id != :currentUserId")
    List<User> findDiscoverableRegularUsersExcludingUser(@Param("currentUserId") Long currentUserId);
}