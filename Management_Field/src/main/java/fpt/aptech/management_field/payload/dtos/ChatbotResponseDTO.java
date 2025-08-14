package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponseDTO {
    private String text;
    private List<ActionDTO> actions;
    private String sessionId;
    private Map<String, Object> context;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionDTO {
        private String label;
        private String type; // "action" or "query"
        private Map<String, Object> payload;
    }
}