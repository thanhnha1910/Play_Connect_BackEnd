package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotRequestDTO {
    private String message;
    private String sessionId;
    private Map<String, Object> context;
    
    // Explicit setter for compatibility
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
}