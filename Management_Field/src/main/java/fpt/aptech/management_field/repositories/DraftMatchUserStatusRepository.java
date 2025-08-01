package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.DraftMatchUserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DraftMatchUserStatusRepository extends JpaRepository<DraftMatchUserStatus, Long> {
    
    @Query("SELECT s FROM DraftMatchUserStatus s WHERE s.draftMatch.id = :draftMatchId AND s.user.id = :userId")
    Optional<DraftMatchUserStatus> findByDraftMatchIdAndUserId(@Param("draftMatchId") Long draftMatchId, @Param("userId") Long userId);
    
    @Query("SELECT s FROM DraftMatchUserStatus s WHERE s.draftMatch.id = :draftMatchId")
    List<DraftMatchUserStatus> findByDraftMatchId(@Param("draftMatchId") Long draftMatchId);
    
    @Query("SELECT s FROM DraftMatchUserStatus s WHERE s.draftMatch.id = :draftMatchId AND s.status = :status")
    List<DraftMatchUserStatus> findByDraftMatchIdAndStatus(@Param("draftMatchId") Long draftMatchId, @Param("status") String status);
    
    @Query("SELECT s FROM DraftMatchUserStatus s WHERE s.user.id = :userId")
    List<DraftMatchUserStatus> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT s FROM DraftMatchUserStatus s WHERE s.user.id = :userId AND s.status = :status")
    List<DraftMatchUserStatus> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    @Query("SELECT COUNT(s) FROM DraftMatchUserStatus s WHERE s.draftMatch.id = :draftMatchId AND s.status = 'APPROVED'")
    Long countApprovedUsersByDraftMatchId(@Param("draftMatchId") Long draftMatchId);
    
    @Query("SELECT COUNT(s) FROM DraftMatchUserStatus s WHERE s.draftMatch.id = :draftMatchId AND s.status = 'PENDING'")
    Long countPendingUsersByDraftMatchId(@Param("draftMatchId") Long draftMatchId);
    
    @Query("SELECT s FROM DraftMatchUserStatus s WHERE s.draftMatch.id = :draftMatchId AND s.status = 'APPROVED'")
    List<DraftMatchUserStatus> findApprovedUsersByDraftMatchId(@Param("draftMatchId") Long draftMatchId);
    
    boolean existsByDraftMatchIdAndUserId(Long draftMatchId, Long userId);
}