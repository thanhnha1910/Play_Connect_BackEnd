package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.Message;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.MessageDTO;
import fpt.aptech.management_field.payload.response.ChatMessageResponseDTO;
import fpt.aptech.management_field.repositories.MessageRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Message sendMessage(MessageDTO messageDTO) {
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(messageDTO.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message message = new Message();
        message.setContent(messageDTO.getContent());
        message.setTimestamp(messageDTO.getTimestamp() != null ? messageDTO.getTimestamp() : LocalDateTime.now());
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setRead(false); // default to unread

        return messageRepository.save(message);
    }

    @Override
    public List<ChatMessageResponseDTO> getConversation(Long user1Id, Long user2Id) {
        List<Message> messages = messageRepository
                .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
                        user1Id, user2Id, user2Id, user1Id
                );

        return messages.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void markMessagesAsRead(Long senderId, Long receiverId) {
        List<Message> unreadMessages = messageRepository
                .findBySenderIdAndReceiverIdAndIsReadFalse(senderId, receiverId);

        for (Message msg : unreadMessages) {
            msg.setRead(true);
        }

        messageRepository.saveAll(unreadMessages);
    }

    private ChatMessageResponseDTO mapToDTO(Message message) {
        ChatMessageResponseDTO dto = new ChatMessageResponseDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFullName());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setReceiverName(message.getReceiver().getFullName());
        return dto;
    }
}
