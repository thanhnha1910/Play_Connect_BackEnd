package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDto {
    private Long id;
    private String ownerName;
    private String action;
    private String description;
    private Instant timestamp;
    private String status;
}