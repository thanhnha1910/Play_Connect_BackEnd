package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.RefreshToken;
import fpt.aptech.management_field.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    
    List<RefreshToken> findByUser(User user);
    
    Optional<RefreshToken> findFirstByUserOrderByExpiryDateDesc(User user);
    
    @Modifying
    @Transactional
    int deleteByUser(User user);
}