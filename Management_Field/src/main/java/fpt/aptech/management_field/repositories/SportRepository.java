package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {
    
    // Find all active sports
    List<Sport> findByIsActiveTrue();
    
    // Find sport by sport code
    Optional<Sport> findBySportCode(String sportCode);
    
    // Find sport by sport code and active status
    Optional<Sport> findBySportCodeAndIsActiveTrue(String sportCode);
    
    // Check if sport code exists
    boolean existsBySportCode(String sportCode);
    
    // Find sports by name containing (case insensitive)
    @Query("SELECT s FROM Sport s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) AND s.isActive = true")
    List<Sport> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
}