package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.Message;
import fpt.aptech.management_field.payload.dtos.MessageDTO;
import fpt.aptech.management_field.payload.response.ChatMessageResponseDTO;
import fpt.aptech.management_field.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // 1. Send a message
    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody MessageDTO messageDTO) {
        Message savedMessage = messageService.sendMessage(messageDTO);
        return ResponseEntity.ok(savedMessage);
    }

    // 2. Get all messages between two users
    @GetMapping("/conversation")
    public ResponseEntity<List<ChatMessageResponseDTO>> getConversation(
            @RequestParam Long user1,
            @RequestParam Long user2
    ) {
        List<ChatMessageResponseDTO> conversation = messageService.getConversation(user1, user2);
        return ResponseEntity.ok(conversation);
    }

    // 3. Mark all messages as read from sender to receiver
    @PostMapping("/mark-as-read")
    public ResponseEntity<Void> markMessagesAsRead(
            @RequestParam Long senderId,
            @RequestParam Long receiverId
    ) {
        messageService.markMessagesAsRead(senderId, receiverId);
        return ResponseEntity.ok().build();
    }
}
