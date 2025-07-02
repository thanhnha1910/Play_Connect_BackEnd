package fpt.aptech.management_field.payload.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ParticipantResponseDTO {
    private Long id;
    private String message;
    private Boolean accepted;
    private int numberOfPeople;
    private String userFullName;
    private String userPhoneNumber;
    private Long recruitmentId;
    private String recruitmentMessage;
    private LocalDateTime recruitmentPlayTime;
}
