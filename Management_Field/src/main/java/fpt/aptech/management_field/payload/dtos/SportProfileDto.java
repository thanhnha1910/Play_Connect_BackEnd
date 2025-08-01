package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SportProfileDto {
    private String sport; // e.g., "BONG_DA", "CAU_LONG"
    private Integer skill; // 1-5 skill level
    private List<String> tags; // e.g., ["tiền đạo", "sút tốt"]
    
    // Explicit getter for compatibility
    public List<String> getTags() {
        return tags;
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class UserSportProfilesDto {
    private Map<String, SportProfileDto> sportProfiles;
    private Boolean isDiscoverable;
}