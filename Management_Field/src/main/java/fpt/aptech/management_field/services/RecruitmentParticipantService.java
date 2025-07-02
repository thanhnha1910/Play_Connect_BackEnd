package fpt.aptech.management_field.services;

import fpt.aptech.management_field.payload.dtos.ParticipantResponseDTO;
import fpt.aptech.management_field.payload.dtos.RecruitmentParticipantDTO;
import fpt.aptech.management_field.models.RecruitmentParticipant;

import java.util.List;

public interface RecruitmentParticipantService {
    boolean updateAcceptance(Long participantId, boolean accepted);
    RecruitmentParticipant join(Long recruitmentId, RecruitmentParticipantDTO dto);
    List<ParticipantResponseDTO> findByRecruitmentId(Long recruitmentId);
}
