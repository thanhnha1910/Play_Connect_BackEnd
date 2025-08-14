package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.ConversationContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class ConversationContextService {
    
    private static final int CONTEXT_TIMEOUT_MINUTES = 30;
    
    @Cacheable(value = "conversationContext", key = "#sessionId")
    public ConversationContext getContext(String sessionId) {
        // If not in cache, create new context
        return new ConversationContext(sessionId);
    }
    
    @CachePut(value = "conversationContext", key = "#context.sessionId")
    public ConversationContext saveContext(ConversationContext context) {
        context.setLastUpdated(LocalDateTime.now());
        return context;
    }
    
    @CacheEvict(value = "conversationContext", key = "#sessionId")
    public void clearContext(String sessionId) {
        // Context will be removed from cache
    }
    
    public ConversationContext updateContext(String sessionId, String intent, Map<String, Object> entities, String message) {
        ConversationContext context = getContext(sessionId);
        context.setCurrentIntent(intent);
        context.setLastMessage(message);
        context.mergeEntities(entities);
        return saveContext(context);
    }
    
    public String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    public boolean isContextExpired(ConversationContext context) {
        if (context == null || context.getLastUpdated() == null) {
            return true;
        }
        return context.getLastUpdated().isBefore(LocalDateTime.now().minusMinutes(CONTEXT_TIMEOUT_MINUTES));
    }
    
    public boolean hasRequiredEntities(ConversationContext context, String... requiredKeys) {
        if (context == null || context.getEntities() == null) {
            return false;
        }
        
        for (String key : requiredKeys) {
            if (!context.hasEntity(key) || context.getEntity(key) == null) {
                return false;
            }
        }
        return true;
    }
}