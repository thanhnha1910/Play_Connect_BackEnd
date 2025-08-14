package fpt.aptech.management_field.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationContext {
    private String sessionId;
    private Map<String, Object> entities;
    private String currentIntent;
    private LocalDateTime lastUpdated;
    private String lastMessage;
    
    public ConversationContext(String sessionId) {
        this.sessionId = sessionId;
        this.entities = new HashMap<>();
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void updateEntity(String key, Object value) {
        if (this.entities == null) {
            this.entities = new HashMap<>();
        }
        this.entities.put(key, value);
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void mergeEntities(Map<String, Object> newEntities) {
        if (this.entities == null) {
            this.entities = new HashMap<>();
        }
        if (newEntities != null) {
            this.entities.putAll(newEntities);
        }
        this.lastUpdated = LocalDateTime.now();
    }
    
    public Object getEntity(String key) {
        return this.entities != null ? this.entities.get(key) : null;
    }
    
    public boolean hasEntity(String key) {
        return this.entities != null && this.entities.containsKey(key);
    }
    
    public void clearEntities() {
        if (this.entities != null) {
            this.entities.clear();
        }
        this.lastUpdated = LocalDateTime.now();
    }
}