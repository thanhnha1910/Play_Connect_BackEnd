package fpt.aptech.management_field.payload.dtos;

import lombok.Data;

@Data
public class RecruitmentParticipantDTO {
    private Long userId;
    private Long recruitmentId;
    private String message;
    private Integer numberOfPeople;
}