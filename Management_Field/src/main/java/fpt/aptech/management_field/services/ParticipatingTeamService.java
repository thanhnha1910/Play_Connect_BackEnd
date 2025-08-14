package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.ParticipatingTeam;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.repositories.ParticipatingTeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipatingTeamService {
    @Autowired
    private ParticipatingTeamRepository participatingTeamRepository;
    
    @Autowired
    private NotificationService notificationService;

    public ParticipatingTeam getById(Long id) {
        return participatingTeamRepository.findById(id).orElse(null);
    }

    public ParticipatingTeam save(ParticipatingTeam team) {
        return participatingTeamRepository.save(team);
    }

    @Transactional
    public ParticipatingTeam confirmRegistration(Long participantId) {
        ParticipatingTeam participant = getById(participantId);
        if (participant == null) {
            throw new RuntimeException("Participating team not found with ID: " + participantId);
        }
        participant.setStatus("CONFIRMED");
        ParticipatingTeam savedParticipant = participatingTeamRepository.save(participant);
        
        // Send notification to field owner
        try {
            User fieldOwner = participant.getTournament().getLocation().getOwner().getUser();
            if (fieldOwner != null) {
                notificationService.createTournamentRegistrationNotificationForOwner(
                    fieldOwner,
                    participant.getTeam().getName(),
                    participant.getTournament().getName(),
                    participant.getTournament().getLocation().getName(),
                    participant.getTournament().getTournamentId()
                );
            }
        } catch (Exception e) {
            // Log error but don't fail the confirmation
            System.err.println("Failed to send tournament registration notification: " + e.getMessage());
        }
        
        return savedParticipant;
    }
}
