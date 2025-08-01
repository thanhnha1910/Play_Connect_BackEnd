package fpt.aptech.management_field.payload.dtos;

import lombok.Data;

@Data
public class RecruitmentPostRequestDTO {
    private Long userId;
    private Integer peopleNeeded;
    private String message;
    
    // Explicit getters for compatibility
    public Long getUserId() {
        return userId;
    }
    
    public Integer getPeopleNeeded() {
        return peopleNeeded;
    }
    
    public String getMessage() {
        return message;
    }
}
