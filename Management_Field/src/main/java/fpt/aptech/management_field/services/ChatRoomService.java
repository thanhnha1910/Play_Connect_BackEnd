package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.ChatRoom;
import fpt.aptech.management_field.models.ChatMember;
import fpt.aptech.management_field.models.ChatMessage;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.models.Notification;
import fpt.aptech.management_field.payload.dtos.ChatMessageDTO;
import fpt.aptech.management_field.payload.dtos.ChatMemberDTO;
import fpt.aptech.management_field.payload.dtos.ChatRoomDTO;
import fpt.aptech.management_field.repositories.ChatRoomRepository;
import fpt.aptech.management_field.repositories.ChatMessageRepository;
import fpt.aptech.management_field.repositories.ChatMemberRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public ChatRoom createChatRoom(String name, Long creatorUserId) {
        if (name == null || name.trim().isEmpty() || name.length() < 3) {
            throw new RuntimeException("Tên phòng chat phải có ít nhất 3 ký tự");
        }
        if (chatRoomRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Tên phòng chat đã tồn tại");
        }
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(name);
        chatRoom.setCreatedAt(LocalDateTime.now());

        // Lưu ChatRoom trước
        chatRoom = chatRoomRepository.save(chatRoom);

        // Tạo ChatMember cho người tạo
        ChatMember creatorMember = new ChatMember();
        creatorMember.setChatRoom(chatRoom);
        creatorMember.setUser(creator);
        creatorMember.setAdmin(true);
        creatorMember.setCreator(true);
        creatorMember.setActive(true);
        creatorMember.setJoinedAt(LocalDateTime.now());
        chatMemberRepository.save(creatorMember);

        // Gửi thông báo qua WebSocket
        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatRoom.getId(),
                new ChatMessageDTO(0L, 0L, "System", "Phòng chat " + chatRoom.getName() + " đã được tạo", LocalDateTime.now()));

        return chatRoom;
    }

    @Transactional
    public void addMember(Long chatRoomId, String phoneNumber, Long adminUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat"));

        // Kiểm tra quyền - bất kỳ thành viên nào cũng có thể thêm người khác
        Optional<ChatMember> memberCheck = chatMemberRepository.findByChatRoomIdAndUserId(chatRoomId, adminUserId);
        if (memberCheck.isEmpty() || !memberCheck.get().isActive()) {
            throw new RuntimeException("Bạn phải là thành viên của phòng chat để thêm người khác");
        }

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với số điện thoại: " + phoneNumber));

        // Kiểm tra xem user đã là thành viên chưa
        Optional<ChatMember> existingMember = chatMemberRepository.findByChatRoomIdAndUserId(chatRoomId, user.getId());
        if (existingMember.isPresent()) {
            throw new RuntimeException("Người dùng đã là thành viên của phòng chat này");
        }

        ChatMember member = new ChatMember();
        member.setChatRoom(chatRoom);
        member.setUser(user);
        member.setAdmin(false);
        member.setActive(true);
        member.setJoinedAt(LocalDateTime.now());

        chatMemberRepository.save(member);

        // Tạo notification cho user được thêm vào
        createNotificationForNewChatMember(chatRoom, user);

        // Gửi thông báo qua WebSocket
        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatRoomId,
                new ChatMessageDTO(0L, user.getId(), user.getFullName(),
                        user.getFullName() + " đã được thêm vào phòng chat", LocalDateTime.now()));
    }

    @Transactional
    public void addMemberByEmail(Long chatRoomId, String email, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat"));

        // Kiểm tra quyền - bất kỳ thành viên nào cũng có thể thêm người khác
        Optional<ChatMember> memberCheck = chatMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        if (memberCheck.isEmpty() || !memberCheck.get().isActive()) {
            throw new RuntimeException("Bạn phải là thành viên của phòng chat để thêm người khác");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        // Kiểm tra xem user đã là thành viên chưa
        Optional<ChatMember> existingMember = chatMemberRepository.findByChatRoomIdAndUserId(chatRoomId, user.getId());
        if (existingMember.isPresent()) {
            throw new RuntimeException("Người dùng đã là thành viên của phòng chat này");
        }

        ChatMember member = new ChatMember();
        member.setChatRoom(chatRoom);
        member.setUser(user);
        member.setAdmin(false);
        member.setActive(true);
        member.setJoinedAt(LocalDateTime.now());

        chatMemberRepository.save(member);

        // Tạo notification cho user được thêm vào
        createNotificationForNewChatMember(chatRoom, user);

        // Gửi thông báo qua WebSocket
        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatRoomId,
                new ChatMessageDTO(0L, user.getId(), user.getFullName(),
                        user.getFullName() + " đã được thêm vào phòng chat", LocalDateTime.now()));
    }

    @Transactional
    public void removeMember(Long chatRoomId, Long userIdToRemove, Long creatorUserId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat"));

        // Kiểm tra quyền creator (người tạo nhóm)
        Optional<ChatMember> creatorMember = chatMemberRepository.findByChatRoomIdAndUserId(chatRoomId, creatorUserId);
        if (creatorMember.isEmpty() || !creatorMember.get().isCreator()) {
            throw new RuntimeException("Chỉ người tạo nhóm mới có thể xóa thành viên");
        }

        Optional<ChatMember> memberToRemove = chatMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userIdToRemove);
        if (memberToRemove.isEmpty()) {
            throw new RuntimeException("Người dùng không phải là thành viên của phòng chat này");
        }

        ChatMember member = memberToRemove.get();
        User removedUser = member.getUser();
        
        chatMemberRepository.delete(member);
        
        // Gửi thông báo qua WebSocket
        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatRoomId,
                new ChatMessageDTO(0L, removedUser.getId(), removedUser.getFullName(), 
                        removedUser.getFullName() + " đã bị xóa khỏi phòng chat bởi admin", LocalDateTime.now()));
    }

    public List<ChatRoom> getUserChatRooms(Long userId) {
        return chatMemberRepository.findActiveChatRoomsByUserId(userId);
    }

    public List<ChatRoomDTO> getUserChatRoomsDTO(Long userId) {
        List<ChatRoom> chatRooms = getUserChatRooms(userId);
        return chatRooms.stream().map(room -> {
            ChatRoomDTO dto = new ChatRoomDTO();
            dto.setId(room.getId());
            dto.setName(room.getName());
            dto.setCreatedAt(room.getCreatedAt());
            
            // Lấy tin nhắn cuối cùng
            ChatMessage lastMessage = room.getLastMessage();
            if (lastMessage != null) {
                dto.setLastMessage(lastMessage.getContent());
                dto.setLastMessageTime(lastMessage.getSentAt());
            }
            
            dto.setMemberCount(room.getActiveMemberCount());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<ChatMemberDTO> getChatRoomMembers(Long chatRoomId) {
        System.out.println("🔍 DEBUG: Getting members for chatRoomId: " + chatRoomId);
        List<ChatMember> members = chatMemberRepository.findActiveMembersByChatRoomId(chatRoomId);
        System.out.println("🔍 DEBUG: Found " + members.size() + " members");
        
        return members.stream().map(member -> {
            ChatMemberDTO dto = new ChatMemberDTO();
            dto.setUserId(member.getUser().getId());
            dto.setUsername(member.getUser().getFullName());
            dto.setEmail(member.getUser().getEmail());
            dto.setAdmin(member.isAdmin());
            dto.setCreator(member.isCreator());
            dto.setJoinedAt(member.getJoinedAt());
            
            System.out.println("🔍 DEBUG: Member - ID: " + member.getUser().getId() + 
                             ", Name: " + member.getUser().getFullName() + 
                             ", isAdmin: " + member.isAdmin() + 
                             ", isCreator: " + member.isCreator());
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public ChatMessage sendMessage(Long chatRoomId, Long userId, String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Kiểm tra xem user có phải là thành viên không
        Optional<ChatMember> member = chatMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        if (member.isEmpty() || !member.get().isActive()) {
            throw new RuntimeException("Bạn không phải là thành viên của phòng chat này");
        }

        ChatMessage message = new ChatMessage();
        message.setChatRoom(chatRoom);
        message.setUser(user);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        message = chatMessageRepository.save(message);

        // Gửi tin nhắn qua WebSocket
        ChatMessageDTO messageDTO = new ChatMessageDTO(
                message.getId(),
                user.getId(),
                user.getFullName(),
                content,
                message.getSentAt()
        );

        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatRoomId, messageDTO);
        return message;
    }

    public List<ChatMessageDTO> getMessages(Long chatRoomId, Long userId) {
        // Kiểm tra xem user có phải là thành viên không
        Optional<ChatMember> member = chatMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        if (member.isEmpty() || !member.get().isActive()) {
            throw new RuntimeException("Bạn không phải là thành viên của phòng chat này");
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);
        return messages.stream()
                .map(m -> new ChatMessageDTO(
                        m.getId(),
                        m.getUser().getId(),
                        m.getUser().getFullName(),
                        m.getContent(),
                        m.getSentAt()
                ))
                .collect(Collectors.toList());
    }

    public boolean isUserMemberOfRoom(Long chatRoomId, Long userId) {
        Optional<ChatMember> member = chatMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        return member.isPresent() && member.get().isActive();
    }

    public boolean isUserAdminOfRoom(Long chatRoomId, Long userId) {
        Optional<ChatMember> member = chatMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        return member.isPresent() && member.get().isAdmin();
    }

    public boolean isUserCreatorOfRoom(Long chatRoomId, Long userId) {
        Optional<ChatMember> member = chatMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        return member.isPresent() && member.get().isCreator();
    }

    @Transactional
    public void leaveChatRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat"));

        Optional<ChatMember> member = chatMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        if (member.isEmpty()) {
            throw new RuntimeException("Bạn không phải là thành viên của phòng chat này");
        }

        ChatMember chatMember = member.get();
        User user = userRepository.findById(userId).orElse(null);
        String userName = user != null ? user.getFullName() : "Người dùng";
        
        // Kiểm tra nếu là creator (người tạo nhóm)
        if (chatMember.isCreator()) {
            // Gửi thông báo trước khi xóa
            messagingTemplate.convertAndSend("/topic/chatrooms/" + chatRoomId,
                    new ChatMessageDTO(0L, 0L, "System", "Phòng chat đã bị xóa do người tạo nhóm rời khỏi", LocalDateTime.now()));
            
            // Nếu là creator, xóa toàn bộ phòng chat
            deleteChatRoomCompletely(chatRoomId);
        } else {
            // Xóa thành viên khỏi phòng chat
            chatMemberRepository.delete(chatMember);
            
            // Gửi thông báo qua WebSocket
            messagingTemplate.convertAndSend("/topic/chatrooms/" + chatRoomId,
                    new ChatMessageDTO(0L, userId, userName, userName + " đã rời khỏi phòng chat", LocalDateTime.now()));
        }
    }

    @Transactional
    public void deleteChatRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat"));

        // Kiểm tra quyền creator (người tạo nhóm)
        boolean isCreator = isUserCreatorOfRoom(chatRoomId, userId);
        if (!isCreator) {
            throw new RuntimeException("Chỉ người tạo nhóm mới có thể xóa phòng chat");
        }

        // Gửi thông báo trước khi xóa
        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatRoomId,
                new ChatMessageDTO(0L, 0L, "System", "Phòng chat sẽ bị xóa bởi người tạo nhóm", LocalDateTime.now()));

        // Xóa toàn bộ phòng chat
        deleteChatRoomCompletely(chatRoomId);
    }

    @Transactional
    public void clearChatHistory(Long chatRoomId, Long requesterId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));

        // Kiểm tra quyền xóa lịch sử (chỉ creator mới được xóa)
        boolean isCreator = isUserCreatorOfRoom(chatRoomId, requesterId);
        if (!isCreator) {
            throw new RuntimeException("Chỉ người tạo phòng mới có thể xóa lịch sử tin nhắn");
        }

        // Xóa tất cả tin nhắn trong phòng
        chatMessageRepository.deleteByChatRoomId(chatRoomId);

        // Gửi thông báo qua WebSocket
        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatRoomId,
                new ChatMessageDTO(0L, 0L, "System", "Lịch sử trò chuyện đã được xóa bởi người tạo phòng.", LocalDateTime.now()));
    }

    @Transactional
    public void clearChatMessages(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat"));

        // Kiểm tra quyền creator (người tạo nhóm)
        boolean isCreator = isUserCreatorOfRoom(chatRoomId, userId);
        if (!isCreator) {
            throw new RuntimeException("Chỉ người tạo nhóm mới có thể xóa tin nhắn");
        }

        // Xóa tất cả tin nhắn
        chatMessageRepository.deleteByChatRoomId(chatRoomId);

        // Gửi thông báo
        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatRoomId,
                new ChatMessageDTO(0L, 0L, "System", "Tất cả tin nhắn đã được xóa bởi người tạo nhóm", LocalDateTime.now()));
    }

    @Transactional
    public void deleteMessage(Long chatRoomId, Long messageId, Long userId) {
        // Kiểm tra phòng chat tồn tại
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat"));

        // Kiểm tra tin nhắn tồn tại
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn"));

        // Kiểm tra tin nhắn thuộc phòng chat này
        if (!message.getChatRoom().getId().equals(chatRoomId)) {
            throw new RuntimeException("Tin nhắn không thuộc phòng chat này");
        }

        // Kiểm tra quyền xóa: người gửi hoặc người tạo nhóm
        boolean isMessageSender = message.getUser().getId().equals(userId);
        boolean isCreator = isUserCreatorOfRoom(chatRoomId, userId);
        
        if (!isMessageSender && !isCreator) {
            throw new RuntimeException("Bạn chỉ có thể xóa tin nhắn của chính mình hoặc nếu bạn là người tạo nhóm");
        }

        // Xóa tin nhắn
        chatMessageRepository.delete(message);

        // Gửi thông báo qua WebSocket
        User user = userRepository.findById(userId).orElse(null);
        String userName = user != null ? user.getFullName() : "Người dùng";
        String notificationMessage = isMessageSender ? 
            userName + " đã xóa tin nhắn của mình" : 
            "Tin nhắn đã bị xóa bởi quản trị viên";
            
        messagingTemplate.convertAndSend("/topic/chatrooms/" + chatRoomId + "/delete",
                Map.of("messageId", messageId, "notification", notificationMessage));
    }

    @Transactional
    private void deleteChatRoomCompletely(Long chatRoomId) {
        // Lấy ChatRoom entity để Hibernate có thể cascade delete đúng cách
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat"));
        
        // Xóa phòng chat - Hibernate sẽ tự động cascade delete messages và members
        // do có annotation cascade = CascadeType.ALL và orphanRemoval = true
        chatRoomRepository.delete(chatRoom);
    }

    private void createNotificationForNewChatMember(ChatRoom chatRoom, User user) {
        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setTitle("Bạn đã được thêm vào phòng chat");
        notification.setContent(String.format("Bạn đã được thêm vào phòng chat '%s'. Hãy tham gia trò chuyện ngay!", 
                chatRoom.getName()));
        notification.setType("CHAT_INVITATION");
        notification.setRelatedEntityId(chatRoom.getId());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        
        notificationService.createNotification(notification);
    }
}