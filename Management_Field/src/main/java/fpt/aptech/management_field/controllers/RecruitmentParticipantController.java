package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.payload.dtos.ParticipantResponseDTO;
import fpt.aptech.management_field.payload.dtos.RecruitmentParticipantDTO;
import fpt.aptech.management_field.payload.dtos.RecruitmentParticipantResponseDTO;
import fpt.aptech.management_field.models.RecruitmentParticipant;
import fpt.aptech.management_field.services.RecruitmentParticipantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/participants")
public class RecruitmentParticipantController {

    private final RecruitmentParticipantService participantService;

    public RecruitmentParticipantController(RecruitmentParticipantService participantService) {
        this.participantService = participantService;
    }

    @PostMapping("/join/{recruitmentId}")
    public ResponseEntity<RecruitmentParticipantResponseDTO> joinRecruitment(
            @PathVariable Long recruitmentId,
            @RequestBody RecruitmentParticipantDTO dto
    ) {
        RecruitmentParticipant saved = participantService.join(recruitmentId, dto);

        RecruitmentParticipantResponseDTO response = new RecruitmentParticipantResponseDTO();
        response.setId(saved.getId());
        response.setMessage(saved.getMessage());
        response.setIsAccepted(saved.getIsAccepted());
        response.setNumberOfPeople(saved.getNumberOfPeople());
        response.setUserFullName(saved.getUser().getFullName());
        response.setUserPhoneNumber(saved.getUser().getPhoneNumber());
        response.setRecruitmentId(saved.getRecruitment().getId());
        response.setRecruitmentMessage(saved.getRecruitment().getMessage());
        response.setRecruitmentPlayTime(saved.getRecruitment().getPlayTime().toString());

        return ResponseEntity.ok(response);
    }

    // ✅ Get all participants for a specific recruitment
    @GetMapping("/recruitment/{recruitmentId}")
    public ResponseEntity<List<ParticipantResponseDTO>> getByRecruitment(@PathVariable Long recruitmentId) {
        List<ParticipantResponseDTO> participants = participantService.findByRecruitmentId(recruitmentId);
        return ResponseEntity.ok(participants);
    }
    // ✅ Accept/Deny participant
    @PutMapping("/{participantId}/accept")
    public ResponseEntity<String> updateAcceptance(
            @PathVariable Long participantId,
            @RequestParam boolean accepted
    ) {
        boolean result = participantService.updateAcceptance(participantId, accepted);
        return ResponseEntity.ok(result
                ? (accepted ? "Participant accepted." : "Participant denied.")
                : "Operation failed.");
    }
}
