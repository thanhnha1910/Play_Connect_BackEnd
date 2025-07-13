package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.Message;
import fpt.aptech.management_field.payload.dtos.MessageDTO;
import fpt.aptech.management_field.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequiredArgsConstructor
public class MessageWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat") // Maps to /app/chat
    public void send(MessageDTO messageDTO) {
        // Save the message to the database
        Message savedMessage = messageService.sendMessage(messageDTO);

        // Send it to the receiver via private queue
        String destination = "/user/" + savedMessage.getReceiver().getId() + "/queue/messages";
        messagingTemplate.convertAndSend(destination, savedMessage);
    }
}
