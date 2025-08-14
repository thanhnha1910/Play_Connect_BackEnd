package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Future;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Request DTO for updating an existing tournament
 * All fields are optional for partial updates
 */
@Data
public class UpdateTournamentRequest {
    
    private String name;
    
    private String description;
    
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;
    
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;
    
    @Min(value = 0, message = "Prize must be non-negative")
    private Integer prize;
    
    @Min(value = 0, message = "Entry fee must be non-negative")
    private Integer entryFee;
    
    @Min(value = 2, message = "Minimum 2 teams required")
    private Integer slots;
    
    private Long locationId;
    
    private String coverImage;
    
    private String status; // upcoming, ongoing, finished, cancelled
}