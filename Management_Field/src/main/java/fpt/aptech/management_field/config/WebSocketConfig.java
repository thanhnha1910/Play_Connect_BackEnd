package fpt.aptech.management_field.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This is the endpoint the client will connect to
        registry.addEndpoint("/ws-chat")
                .setAllowedOrigins("*")  // Allow any frontend for now (you can restrict this in prod)
                .withSockJS();           // Use SockJS as fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enables a simple in-memory message broker
        registry.enableSimpleBroker("/user"); // Enables destinations like /user/queue/messages
        registry.setApplicationDestinationPrefixes("/app"); // Client will send messages to /app/chat
        registry.setUserDestinationPrefix("/user"); // For private messaging
    }
}
