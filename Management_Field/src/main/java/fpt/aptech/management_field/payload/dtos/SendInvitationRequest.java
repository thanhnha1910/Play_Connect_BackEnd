package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendInvitationRequest {
    private Long inviteeId;
    private Long openMatchId;
    
    // Explicit getters for compatibility
    public Long getInviteeId() {
        return inviteeId;
    }
    
    public Long getOpenMatchId() {
        return openMatchId;
    }
}