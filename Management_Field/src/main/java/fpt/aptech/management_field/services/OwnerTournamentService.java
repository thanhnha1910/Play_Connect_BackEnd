package fpt.aptech.management_field.services;

import fpt.aptech.management_field.payload.request.CreateTournamentRequest;
import fpt.aptech.management_field.payload.request.UpdateTournamentRequest;
import fpt.aptech.management_field.payload.response.TournamentDetailResponse;
import fpt.aptech.management_field.payload.dtos.TournamentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for owner tournament management operations
 */
public interface OwnerTournamentService {
    
    /**
     * Get all tournaments for a specific owner with optional filtering
     * @param ownerId the owner ID
     * @param status tournament status filter (optional)
     * @param locationId location filter (optional)
     * @param pageable pagination information
     * @return page of tournaments
     */
    Page<TournamentDto> getOwnerTournaments(Long ownerId, String status, Long locationId, Pageable pageable);
    
    /**
     * Get detailed tournament information including participating teams and financials
     * @param tournamentId the tournament ID
     * @param ownerId the owner ID (for authorization)
     * @return tournament detail response
     */
    TournamentDetailResponse getTournamentDetail(Long tournamentId, Long ownerId);
    
    /**
     * Create a new tournament
     * @param request tournament creation request
     * @param ownerId the owner ID
     * @return created tournament DTO
     */
    TournamentDto createTournament(CreateTournamentRequest request, Long ownerId);
    
    /**
     * Update an existing tournament
     * @param tournamentId the tournament ID
     * @param request tournament update request
     * @param ownerId the owner ID (for authorization)
     * @return updated tournament DTO
     */
    TournamentDto updateTournament(Long tournamentId, UpdateTournamentRequest request, Long ownerId);
    
    /**
     * Delete/Cancel a tournament
     * @param tournamentId the tournament ID
     * @param ownerId the owner ID (for authorization)
     */
    void deleteTournament(Long tournamentId, Long ownerId);
}