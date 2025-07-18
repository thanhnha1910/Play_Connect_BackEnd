package fpt.aptech.management_field.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpsertFieldRequest {
    @NotBlank(message = "Field name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Field type is required")
    private Long typeId;
    
    @NotNull(message = "Location is required")
    private Long locationId;
    
    private Boolean isActive = true;
    
    @Positive(message = "Hourly rate must be positive")
    private Integer hourlyRate;
}