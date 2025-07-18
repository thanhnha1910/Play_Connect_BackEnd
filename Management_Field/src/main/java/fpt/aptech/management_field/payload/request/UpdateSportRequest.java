package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSportRequest {
    @NotBlank
    @Size(max = 100)
    private String name;
    
    @NotBlank
    @Size(max = 50)
    private String sportCode;
    
    @Size(max = 10)
    private String icon;
    
    @NotNull
    private Boolean isActive;
}