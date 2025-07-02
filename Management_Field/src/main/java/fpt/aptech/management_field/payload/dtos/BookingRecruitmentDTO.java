package fpt.aptech.management_field.payload.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingRecruitmentDTO {
    private Long id;
    private String userName;
    private String phone;
    private String fieldNumber;
    private String fieldLocation;
    private LocalDateTime playTime;
    private Integer peopleNeeded;
    private String message;
    private LocalDateTime createdAt; // for response

    private int participantCount;
}
