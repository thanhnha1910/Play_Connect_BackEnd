package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiResponseV2DTO {
    private String intent;
    private Map<String, Object> entities;
    private Double confidence;
    
    // Explicit getters for compatibility
    public String getIntent() {
        return intent;
    }
    
    public Map<String, Object> getEntities() {
        return entities;
    }
    
    public Double getConfidence() {
        return confidence;
    }
}