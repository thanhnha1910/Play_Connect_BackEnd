package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpsertFieldTypeRequest {
    @NotBlank(message = "Field type name is required")
    private String name;
    
    @NotNull(message = "Team capacity is required")
    @Positive(message = "Team capacity must be positive")
    private Integer teamCapacity;
    
    @NotNull(message = "Max capacity is required")
    @Positive(message = "Max capacity must be positive")
    private Integer maxCapacity;
    
    @NotNull(message = "Hourly rate is required")
    @Positive(message = "Hourly rate must be positive")
    private Integer hourlyRate;
    
    private String description;
    
    // Explicit getters for compatibility
    public String getName() {
        return name;
    }
    
    public Integer getTeamCapacity() {
        return teamCapacity;
    }
    
    public Integer getMaxCapacity() {
        return maxCapacity;
    }
    
    public Integer getHourlyRate() {
        return hourlyRate;
    }
    
    public String getDescription() {
        return description;
    }
}