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
}