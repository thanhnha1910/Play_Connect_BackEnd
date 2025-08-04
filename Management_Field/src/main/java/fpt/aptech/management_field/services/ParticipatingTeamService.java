package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.ParticipatingTeam;
import fpt.aptech.management_field.repositories.ParticipatingTeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParticipatingTeamService {
    @Autowired
    private ParticipatingTeamRepository participatingTeamRepository;

    public ParticipatingTeam getById(Long id) {
        return participatingTeamRepository.findById(id).orElse(null);
    }

    public ParticipatingTeam save(ParticipatingTeam team) {
        return participatingTeamRepository.save(team);
    }

    public ParticipatingTeam confirmRegistration(Long participantId) {
        ParticipatingTeam participant = getById(participantId);
        if (participant == null) {
            throw new RuntimeException("Participating team not found with ID: " + participantId);
        }
        participant.setStatus("CONFIRMED");
        return participatingTeamRepository.save(participant);
    }
}
