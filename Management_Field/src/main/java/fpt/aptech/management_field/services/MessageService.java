package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.Message;
import fpt.aptech.management_field.payload.dtos.MessageDTO;
import fpt.aptech.management_field.payload.response.ChatMessageResponseDTO;

import java.util.List;

public interface MessageService {
    Message sendMessage(MessageDTO messageDTO);
    List<ChatMessageResponseDTO> getConversation(Long user1Id, Long user2Id);
    void markMessagesAsRead(Long senderId, Long receiverId);
}
