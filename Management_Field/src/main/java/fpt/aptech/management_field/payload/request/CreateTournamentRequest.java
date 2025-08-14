package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Future;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a new tournament
 * Contains all necessary fields for tournament creation
 */
@Data
public class CreateTournamentRequest {
    
    @NotBlank(message = "Tournament name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;
    
    @NotNull(message = "Prize amount is required")
    @Min(value = 0, message = "Prize must be non-negative")
    private Integer prize;
    
    @NotNull(message = "Entry fee is required")
    @Min(value = 0, message = "Entry fee must be non-negative")
    private Integer entryFee;
    
    @NotNull(message = "Number of slots is required")
    @Min(value = 2, message = "Minimum 2 teams required")
    private Integer slots;
    
    @NotNull(message = "Location ID is required")
    private Long locationId;
    
    private String coverImage;
}