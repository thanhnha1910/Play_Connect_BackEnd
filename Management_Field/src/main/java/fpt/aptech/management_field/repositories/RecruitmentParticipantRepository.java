package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.RecruitmentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecruitmentParticipantRepository extends JpaRepository<RecruitmentParticipant, Long> {

    List<RecruitmentParticipant> findByRecruitmentId(Long recruitmentId);

    List<RecruitmentParticipant> findByUserId(Long userId);
}
