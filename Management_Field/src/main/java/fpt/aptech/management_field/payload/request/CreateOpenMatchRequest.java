package fpt.aptech.management_field.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOpenMatchRequest {
    private Long bookingId;
    private String sportType;
    private Integer slotsNeeded;
    private List<String> requiredTags;
    
    // Explicit getters for compatibility
    public Long getBookingId() {
        return bookingId;
    }
    
    public String getSportType() {
        return sportType;
    }
    
    public Integer getSlotsNeeded() {
        return slotsNeeded;
    }
    
    public List<String> getRequiredTags() {
        return requiredTags;
    }
}