package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Tag;
import fpt.aptech.management_field.models.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    List<Tag> findByIsActiveTrue();
    
    List<Tag> findBySportAndIsActiveTrue(Sport sport);
    
    List<Tag> findBySportIdAndIsActiveTrue(Long sportId);
    
    Optional<Tag> findByNameAndSport(String name, Sport sport);
    
    Optional<Tag> findByNameAndSportId(String name, Long sportId);
    
    @Query("SELECT t FROM Tag t WHERE t.sport.id = :sportId AND t.isActive = true ORDER BY t.name ASC")
    List<Tag> findActiveBySportIdOrderByName(@Param("sportId") Long sportId);
    
    @Query("SELECT t FROM Tag t WHERE t.isActive = true ORDER BY t.name ASC")
    List<Tag> findAllActiveOrderByName();
    
    boolean existsByNameAndSportId(String name, Long sportId);
}