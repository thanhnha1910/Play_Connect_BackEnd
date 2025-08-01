package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiRequestDTO {
    private String message;
    
    // Explicit setter for compatibility
    public void setMessage(String message) {
        this.message = message;
    }
}