package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.DraftMatch;
import fpt.aptech.management_field.models.DraftMatchStatus;
import fpt.aptech.management_field.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DraftMatch entity
 * Provides data access methods for Draft Matches
 */
@Repository
public interface DraftMatchRepository extends JpaRepository<DraftMatch, Long> {
    
    /**
     * Find all draft matches by status, ordered by creation date descending
     */
    List<DraftMatch> findByStatusOrderByCreatedAtDesc(DraftMatchStatus status);
    
    /**
     * Find all draft matches by sport type and status, ordered by creation date descending
     */
    List<DraftMatch> findBySportTypeAndStatusOrderByCreatedAtDesc(String sportType, DraftMatchStatus status);
    
    /**
     * Find all draft matches created by a specific user, ordered by creation date descending
     */
    List<DraftMatch> findByCreatorOrderByCreatedAtDesc(User creator);
    
    /**
     * Find all draft matches created by a specific user with a specific status
     */
    List<DraftMatch> findByCreatorAndStatusOrderByCreatedAtDesc(User creator, DraftMatchStatus status);
    
    /**
     * Find all active draft matches for a specific sport type
     */
    List<DraftMatch> findBySportTypeAndStatus(String sportType, DraftMatchStatus status);
    
    /**
     * Find all draft matches created within a time range
     */
    List<DraftMatch> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find all draft matches with estimated start time within a range
     */
    List<DraftMatch> findByEstimatedStartTimeBetweenAndStatusOrderByEstimatedStartTimeAsc(
        LocalDateTime startTime, LocalDateTime endTime, DraftMatchStatus status);
    
    /**
     * Find paginated draft matches by status
     */
    Page<DraftMatch> findByStatusOrderByCreatedAtDesc(DraftMatchStatus status, Pageable pageable);
    
    /**
     * Find paginated draft matches by sport type and status
     */
    Page<DraftMatch> findBySportTypeAndStatusOrderByCreatedAtDesc(
        String sportType, DraftMatchStatus status, Pageable pageable);
    
    /**
     * Count draft matches by creator
     */
    long countByCreator(User creator);
    
    /**
     * Count draft matches by creator and status
     */
    long countByCreatorAndStatus(User creator, DraftMatchStatus status);
    
    /**
     * Count draft matches by status
     */
    long countByStatus(DraftMatchStatus status);
    
    /**
     * Count draft matches by sport type and status
     */
    long countBySportTypeAndStatus(String sportType, DraftMatchStatus status);
    
    /**
     * Find draft matches that need AI score updates (active matches)
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.status = :status")
    List<DraftMatch> findDraftMatchesNeedingAiUpdate(
        @Param("status") DraftMatchStatus status, 
        @Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find draft matches with AI scores for ranking
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.status = :status ORDER BY dm.createdAt DESC")
    List<DraftMatch> findDraftMatchesWithAiScores(@Param("status") DraftMatchStatus status);
    
    /**
     * Find draft matches by location description (case insensitive search)
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.status = :status AND " +
           "LOWER(dm.locationDescription) LIKE LOWER(CONCAT('%', :location, '%')) " +
           "ORDER BY dm.createdAt DESC")
    List<DraftMatch> findByLocationContainingIgnoreCase(
        @Param("location") String location, 
        @Param("status") DraftMatchStatus status);
    
    /**
     * Find draft matches by skill level
     */
    List<DraftMatch> findBySkillLevelAndStatusOrderByCreatedAtDesc(String skillLevel, DraftMatchStatus status);
    
    /**
     * Find draft matches with slots needed greater than or equal to specified value
     */
    List<DraftMatch> findBySlotsNeededGreaterThanEqualAndStatusOrderByCreatedAtDesc(
        Integer slotsNeeded, DraftMatchStatus status);
    
    /**
     * Find draft matches created by users other than the specified user
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.creator.id != :userId AND dm.status = :status " +
           "ORDER BY dm.createdAt DESC")
    List<DraftMatch> findByCreatorNotAndStatus(
        @Param("userId") Long userId, 
        @Param("status") DraftMatchStatus status);
    
    /**
     * Find draft matches that are ready to convert (have interested users)
     */
    @Query("SELECT DISTINCT dm FROM DraftMatch dm " +
           "JOIN dm.interestedUsers iu " +
           "WHERE dm.status = :status " +
           "ORDER BY dm.createdAt DESC")
    List<DraftMatch> findReadyToConvert(@Param("status") DraftMatchStatus status);
    
    /**
     * Find draft matches with specific number of interested users
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.status = :status AND " +
           "SIZE(dm.interestedUsers) = :count " +
           "ORDER BY dm.createdAt DESC")
    List<DraftMatch> findByInterestedUsersCount(
        @Param("status") DraftMatchStatus status, 
        @Param("count") long count);
    
    /**
     * Find draft matches with minimum number of interested users
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.status = :status AND " +
           "SIZE(dm.interestedUsers) >= :minCount " +
           "ORDER BY dm.createdAt DESC")
    List<DraftMatch> findByMinInterestedUsersCount(
        @Param("status") DraftMatchStatus status, 
        @Param("minCount") long minCount);
    
    /**
     * Find draft matches with minimum interested users count
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.status = :status AND " +
           "SIZE(dm.interestedUsers) >= :minAccepted " +
           "ORDER BY dm.createdAt DESC")
    List<DraftMatch> findByMinAcceptedUsersCount(
        @Param("status") DraftMatchStatus status, 
        @Param("minAccepted") long minAccepted);
    
    /**
     * Find draft matches by multiple sport types
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.sportType IN :sportTypes AND dm.status = :status " +
           "ORDER BY dm.createdAt DESC")
    List<DraftMatch> findBySportTypesAndStatus(
        @Param("sportTypes") List<String> sportTypes, 
        @Param("status") DraftMatchStatus status);
    
    /**
     * Find draft matches created in the last N hours
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.status = :status AND " +
           "dm.createdAt >= :cutoffTime ORDER BY dm.createdAt DESC")
    List<DraftMatch> findRecentDraftMatches(
        @Param("status") DraftMatchStatus status, 
        @Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find draft matches that might be expired (created more than N hours ago and still active)
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.status = 'ACTIVE' AND " +
           "dm.createdAt < :cutoffTime ORDER BY dm.createdAt ASC")
    List<DraftMatch> findPotentiallyExpiredDraftMatches(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Search draft matches by keyword in sport type or location
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.status = :status AND " +
           "(LOWER(dm.sportType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(dm.locationDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY dm.createdAt DESC")
    List<DraftMatch> searchByKeyword(
        @Param("keyword") String keyword, 
        @Param("status") DraftMatchStatus status);
    
    /**
     * Find draft matches with complex filtering
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE " +
           "(:sportType IS NULL OR dm.sportType = :sportType) AND " +
           "(:skillLevel IS NULL OR dm.skillLevel = :skillLevel) AND " +
           "(:minSlots IS NULL OR dm.slotsNeeded >= :minSlots) AND " +
           "(:maxSlots IS NULL OR dm.slotsNeeded <= :maxSlots) AND " +
           "dm.status = :status AND " +
           "dm.creator.id != :excludeUserId " +
           "ORDER BY dm.createdAt DESC")
    List<DraftMatch> findWithFilters(
        @Param("sportType") String sportType,
        @Param("skillLevel") String skillLevel,
        @Param("minSlots") Integer minSlots,
        @Param("maxSlots") Integer maxSlots,
        @Param("status") DraftMatchStatus status,
        @Param("excludeUserId") Long excludeUserId);
    
    /**
     * Get statistics for dashboard
     */
    @Query("SELECT " +
           "COUNT(dm) as totalCount, " +
           "COUNT(CASE WHEN dm.status = 'ACTIVE' THEN 1 END) as activeCount, " +
           "COUNT(CASE WHEN dm.status = 'CONVERTED' THEN 1 END) as convertedCount, " +
           "COUNT(CASE WHEN dm.status = 'CANCELLED' THEN 1 END) as cancelledCount " +
           "FROM DraftMatch dm WHERE dm.createdAt >= :startDate")
    Object[] getDraftMatchStatistics(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Check if user has any active draft matches
     */
    boolean existsByCreatorAndStatus(User creator, DraftMatchStatus status);
    
    /**
     * Check if user has expressed interest in any draft match
     */
    @Query("SELECT COUNT(dm) > 0 FROM DraftMatch dm " +
           "JOIN dm.interestedUsers iu " +
           "WHERE iu.id = :userId AND dm.status = :status")
    boolean hasUserExpressedInterestInAnyDraftMatch(
        @Param("userId") Long userId, 
        @Param("status") DraftMatchStatus status);
    
    /**
     * Find draft match by ID with creator check
     */
    Optional<DraftMatch> findByIdAndCreator(Long id, User creator);
    
    /**
     * Delete old draft matches (cleanup job)
     */
    @Query("DELETE FROM DraftMatch dm WHERE dm.status IN ('CANCELLED', 'EXPIRED', 'CONVERTED') " +
           "AND dm.createdAt < :cutoffTime")
    void deleteOldDraftMatches(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find draft matches by creator ID
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.creator.id = :creatorId ORDER BY dm.createdAt DESC")
    List<DraftMatch> findByCreatorId(@Param("creatorId") Long creatorId);
    
    /**
     * Find active draft matches by sport type
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.sportType = :sportType AND dm.status IN ('RECRUITING', 'FULL') ORDER BY dm.createdAt DESC")
    List<DraftMatch> findActiveDraftMatchesBySportType(@Param("sportType") String sportType);
    
    /**
     * Find all active draft matches
     */
    @Query("SELECT dm FROM DraftMatch dm WHERE dm.status IN ('RECRUITING', 'FULL') ORDER BY dm.createdAt DESC")
    List<DraftMatch> findAllActiveDraftMatches();
}