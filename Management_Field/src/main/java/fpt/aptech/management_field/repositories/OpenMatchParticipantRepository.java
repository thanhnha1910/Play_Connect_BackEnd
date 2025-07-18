package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.OpenMatchParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpenMatchParticipantRepository extends JpaRepository<OpenMatchParticipant, Long> {
    
    @Query("SELECT omp FROM OpenMatchParticipant omp WHERE omp.openMatch.id = :openMatchId AND omp.user.id = :userId")
    Optional<OpenMatchParticipant> findByOpenMatchIdAndUserId(@Param("openMatchId") Long openMatchId, @Param("userId") Long userId);
    
    @Query("SELECT omp FROM OpenMatchParticipant omp WHERE omp.openMatch.id = :openMatchId")
    List<OpenMatchParticipant> findByOpenMatchId(@Param("openMatchId") Long openMatchId);
    
    @Query("SELECT omp FROM OpenMatchParticipant omp WHERE omp.user.id = :userId")
    List<OpenMatchParticipant> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(omp) FROM OpenMatchParticipant omp WHERE omp.openMatch.id = :openMatchId")
    Long countByOpenMatchId(@Param("openMatchId") Long openMatchId);
}