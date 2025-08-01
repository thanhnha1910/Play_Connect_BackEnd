package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Invitation;
import fpt.aptech.management_field.models.InvitationStatus;
import fpt.aptech.management_field.models.InvitationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    
    // Get received invitations for a user (where user is invitee)
    @Query("SELECT i FROM Invitation i WHERE i.invitee.id = :userId AND i.status = :status ORDER BY i.createdAt DESC")
    List<Invitation> findReceivedInvitationsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") InvitationStatus status);
    
    // Get sent invitations for a user (where user is inviter)
    @Query("SELECT i FROM Invitation i WHERE i.inviter.id = :userId ORDER BY i.createdAt DESC")
    List<Invitation> findSentInvitationsByUserId(@Param("userId") Long userId);
    
    // Check if invitation already exists between two users for a specific match
    @Query("SELECT i FROM Invitation i WHERE i.inviter.id = :inviterId AND i.invitee.id = :inviteeId AND i.openMatch.id = :openMatchId")
    Optional<Invitation> findByInviterAndInviteeAndOpenMatch(@Param("inviterId") Long inviterId, @Param("inviteeId") Long inviteeId, @Param("openMatchId") Long openMatchId);
    
    // Get all pending invitations for a specific open match
    @Query("SELECT i FROM Invitation i WHERE i.openMatch.id = :openMatchId AND i.status = 'PENDING'")
    List<Invitation> findPendingInvitationsByOpenMatchId(@Param("openMatchId") Long openMatchId);
    
    // Get received invitations with pending status
    @Query("SELECT i FROM Invitation i WHERE i.invitee.id = :userId AND i.status = 'PENDING' ORDER BY i.createdAt DESC")
    List<Invitation> findPendingReceivedInvitations(@Param("userId") Long userId);
    
    // Get received join requests (where user is invitee and type is REQUEST)
    @Query("SELECT i FROM Invitation i WHERE i.invitee.id = :userId AND i.type = 'REQUEST' AND i.status = 'PENDING' ORDER BY i.createdAt DESC")
    List<Invitation> findPendingReceivedJoinRequests(@Param("userId") Long userId);
    
    // Get sent join requests (where user is inviter and type is REQUEST)
    @Query("SELECT i FROM Invitation i WHERE i.inviter.id = :userId AND i.type = 'REQUEST' ORDER BY i.createdAt DESC")
    List<Invitation> findSentJoinRequestsByUserId(@Param("userId") Long userId);
    
    // UNIFIED QUERIES - Single source of truth for frontend
    
    // Get all received items (invitations where user is invitee + join requests where user is match creator)
    @Query("SELECT i FROM Invitation i WHERE " +
           "(i.invitee.id = :userId AND i.type = 'INVITATION') OR " +
           "(i.invitee.id = :userId AND i.type = 'REQUEST') " +
           "ORDER BY i.createdAt DESC")
    List<Invitation> findAllReceivedItems(@Param("userId") Long userId);
    
    // Get all sent items (invitations where user is inviter + join requests where user is requester)
    @Query("SELECT i FROM Invitation i WHERE " +
           "(i.inviter.id = :userId AND i.type = 'INVITATION') OR " +
           "(i.inviter.id = :userId AND i.type = 'REQUEST') " +
           "ORDER BY i.createdAt DESC")
    List<Invitation> findAllSentItems(@Param("userId") Long userId);
}