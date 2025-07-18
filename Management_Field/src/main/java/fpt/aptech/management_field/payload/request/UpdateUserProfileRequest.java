package fpt.aptech.management_field.payload.request;

import fpt.aptech.management_field.payload.dtos.SportProfileDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {
    private String fullName;
    private String phoneNumber;
    private String address;
    private Map<String, SportProfileDto> sportProfiles;
    private Boolean isDiscoverable;
}