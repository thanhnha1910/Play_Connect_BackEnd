package fpt.aptech.management_field.services;

import fpt.aptech.management_field.payload.dtos.ParticipantResponseDTO;
import fpt.aptech.management_field.payload.dtos.RecruitmentParticipantDTO;
import fpt.aptech.management_field.models.BookingRequirement;
import fpt.aptech.management_field.models.RecruitmentParticipant;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.repositories.BookingRecruitmentRepository;
import fpt.aptech.management_field.repositories.RecruitmentParticipantRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecruitmentParticipantServiceImpl implements RecruitmentParticipantService {

    private final RecruitmentParticipantRepository participantRepo;
    private final BookingRecruitmentRepository recruitmentRepo;
    private final UserRepository userRepo;

    public RecruitmentParticipantServiceImpl(
            RecruitmentParticipantRepository participantRepo,
            BookingRecruitmentRepository recruitmentRepo,
            UserRepository userRepo
    ) {
        this.participantRepo = participantRepo;
        this.recruitmentRepo = recruitmentRepo;
        this.userRepo = userRepo;
    }

    @Override
    public RecruitmentParticipant join(Long recruitmentId, RecruitmentParticipantDTO dto) {
        BookingRequirement recruitment = recruitmentRepo.findById(recruitmentId)
                .orElseThrow(() -> new RuntimeException("Recruitment not found"));

        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RecruitmentParticipant participant = new RecruitmentParticipant();
        participant.setUser(user);
        participant.setRecruitment(recruitment);
        participant.setMessage(dto.getMessage());
        participant.setNumberOfPeople(dto.getNumberOfPeople());
        participant.setIsAccepted(null);
        return participantRepo.save(participant);
    }

    @Override
    public List<ParticipantResponseDTO> findByRecruitmentId(Long recruitmentId) {
        List<RecruitmentParticipant> participants = participantRepo.findByRecruitmentId(recruitmentId);

        return participants.stream().map(p -> {
            ParticipantResponseDTO dto = new ParticipantResponseDTO();
            dto.setId(p.getId());
            dto.setMessage(p.getMessage());
            dto.setAccepted(p.getIsAccepted()); // Allow null
            dto.setNumberOfPeople(p.getNumberOfPeople());

            // Null safety for user
            if (p.getUser() != null) {
                dto.setUserFullName(p.getUser().getFullName());
                dto.setUserPhoneNumber(p.getUser().getPhoneNumber());
            }

            // Null safety for recruitment
            if (p.getRecruitment() != null) {
                dto.setRecruitmentId(p.getRecruitment().getId());
                dto.setRecruitmentMessage(p.getRecruitment().getMessage());
                dto.setRecruitmentPlayTime(p.getRecruitment().getPlayTime());
            }

            return dto;
        }).toList();
    }


    @Override
    public boolean updateAcceptance(Long participantId, boolean accepted) {
        RecruitmentParticipant participant = participantRepo.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        // 🔒 Prevent accepting a previously denied participant
        if (Boolean.FALSE.equals(participant.getIsAccepted()) && accepted) {
            throw new IllegalStateException("Cannot accept a denied participant.");
        }

        participant.setIsAccepted(accepted);
        participantRepo.save(participant);
        return true;
    }

}
