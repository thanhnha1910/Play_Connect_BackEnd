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
    
    // Explicit setters for compatibility
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public void setFieldNumber(String fieldNumber) {
        this.fieldNumber = fieldNumber;
    }
    
    public void setFieldLocation(String fieldLocation) {
        this.fieldLocation = fieldLocation;
    }
    
    public void setPlayTime(LocalDateTime playTime) {
        this.playTime = playTime;
    }
    
    public void setPeopleNeeded(Integer peopleNeeded) {
        this.peopleNeeded = peopleNeeded;
    }
    
    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
