package fpt.aptech.management_field.payload.dtos;

import fpt.aptech.management_field.models.InvitationStatus;
import fpt.aptech.management_field.models.InvitationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDto {
    private Long id;
    private Long inviterId;
    private String inviterName;
    private String inviterProfilePicture;
    private Long inviteeId;
    private String inviteeName;
    private String inviteeProfilePicture;
    private Long openMatchId;
    private String openMatchTitle;
    private String sportType;
    private String fieldName;
    private LocalDateTime matchDateTime;
    private InvitationType type;
    private InvitationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Explicit setters for compatibility
    public void setInviterId(Long inviterId) {
        this.inviterId = inviterId;
    }
    
    public void setInviterName(String inviterName) {
        this.inviterName = inviterName;
    }
    
    public void setInviterProfilePicture(String inviterProfilePicture) {
        this.inviterProfilePicture = inviterProfilePicture;
    }
    
    public void setInviteeId(Long inviteeId) {
        this.inviteeId = inviteeId;
    }
    
    public void setInviteeName(String inviteeName) {
        this.inviteeName = inviteeName;
    }
    
    public void setInviteeProfilePicture(String inviteeProfilePicture) {
        this.inviteeProfilePicture = inviteeProfilePicture;
    }
    
    public void setOpenMatchId(Long openMatchId) {
        this.openMatchId = openMatchId;
    }
    
    public void setOpenMatchTitle(String openMatchTitle) {
        this.openMatchTitle = openMatchTitle;
    }
    
    public void setSportType(String sportType) {
        this.sportType = sportType;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public void setMatchDateTime(LocalDateTime matchDateTime) {
        this.matchDateTime = matchDateTime;
    }
    
    public void setType(InvitationType type) {
        this.type = type;
    }
    
    public void setStatus(InvitationStatus status) {
        this.status = status;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
}