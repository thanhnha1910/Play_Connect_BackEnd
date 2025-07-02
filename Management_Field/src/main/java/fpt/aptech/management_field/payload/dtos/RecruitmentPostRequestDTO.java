package fpt.aptech.management_field.payload.dtos;

import lombok.Data;

@Data
public class RecruitmentPostRequestDTO {
    private Long userId;
    private Integer peopleNeeded;
    private String message;
}
