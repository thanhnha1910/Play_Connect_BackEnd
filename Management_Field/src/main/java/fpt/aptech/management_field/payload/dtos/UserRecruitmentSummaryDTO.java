package fpt.aptech.management_field.payload.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserRecruitmentSummaryDTO {
    private Long recruitmentId;
    private String recruitmentMessage;
    private LocalDateTime recruitmentPlayTime;
    private Integer peopleNeeded;

    private Long userId;
    private String userFullName;
    private String userPhoneNumber;

    private int participantCount;
}
