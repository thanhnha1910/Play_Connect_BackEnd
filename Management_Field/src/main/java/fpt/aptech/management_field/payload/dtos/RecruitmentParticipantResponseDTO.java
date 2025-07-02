package fpt.aptech.management_field.payload.dtos;

import lombok.Data;

@Data
public class RecruitmentParticipantResponseDTO {
    private Long id;
    private String message;
    private Boolean isAccepted;
    private Integer numberOfPeople;

    private String userFullName;
    private String userPhoneNumber;

    private Long recruitmentId;
    private String recruitmentMessage;
    private String recruitmentPlayTime;
}
