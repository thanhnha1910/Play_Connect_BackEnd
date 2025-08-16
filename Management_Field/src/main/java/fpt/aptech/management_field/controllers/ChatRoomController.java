package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.ChatRoom;
import fpt.aptech.management_field.models.ChatMessage;
import fpt.aptech.management_field.payload.dtos.ChatRoomDTO;
import fpt.aptech.management_field.payload.dtos.ChatMessageDTO;
import fpt.aptech.management_field.payload.dtos.ChatMemberDTO;
import fpt.aptech.management_field.payload.dtos.ChatRoomDTO;
import fpt.aptech.management_field.security.services.UserDetailsImpl;
import fpt.aptech.management_field.services.ChatRoomService;
import fpt.aptech.management_field.payload.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/chat-rooms")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    // Tạo phòng chat mới
    @PostMapping
    @PreAuthorize("hasRole('USER') ")
    public ResponseEntity<?> createChatRoom(@RequestBody CreateChatRoomRequest request) {
        try {
            ChatRoom chatRoom = chatRoomService.createChatRoom(request.getName(), request.getCreatorUserId());
            ChatRoomDTO response = new ChatRoomDTO(chatRoom.getId(), chatRoom.getName(), 
                    chatRoom.getCreatedAt(), null, null, 1);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi tạo phòng chat: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // // Thêm thành viên vào phòng chat
    // @PostMapping("/{chatRoomId}/add-member")
    // @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    // public ResponseEntity<?> addMember(@PathVariable Long chatRoomId, @RequestBody AddMemberRequest request) {
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    //     try {
    //         chatRoomService.addMember(chatRoomId, request.getPhoneNumber(), userDetails.getId());
    //         return ResponseEntity.ok(new MessageResponse("Thêm thành viên thành công"));
    //     } catch (RuntimeException e) {
    //         return ResponseEntity.badRequest().body(new MessageResponse("Lỗi thêm thành viên: " + e.getMessage()));
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body(new MessageResponse("Lỗi hệ thống: " + e.getMessage()));
    //     }
    // }


    
    // Thêm thành viên vào phòng chat bằng email
    @PostMapping("/{chatRoomId}/members/by-email")
    @PreAuthorize("hasRole('USER') ")
    public ResponseEntity<?> addMemberByEmail(@PathVariable Long chatRoomId, @RequestBody AddMemberByEmailRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            chatRoomService.addMemberByEmail(chatRoomId, request.getEmail(), userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Thêm thành viên thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi thêm thành viên: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // Xóa thành viên khỏi phòng chat (chỉ người tạo nhóm)
    @DeleteMapping("/{chatRoomId}/remove-member/{userId}")
    @PreAuthorize("hasRole('USER') ")
    public ResponseEntity<?> removeMember(@PathVariable Long chatRoomId, @PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            chatRoomService.removeMember(chatRoomId, userId, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Xóa thành viên thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi xóa thành viên: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // Lấy danh sách phòng chat của user hiện tại
    @GetMapping
    @PreAuthorize("hasRole('USER') ")
    public ResponseEntity<?> getCurrentUserChatRooms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            List<ChatRoomDTO> chatRooms = chatRoomService.getUserChatRoomsDTO(userDetails.getId());
            return ResponseEntity.ok(chatRooms);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi lấy danh sách phòng chat: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // Lấy danh sách phòng chat của user cụ thể (chỉ admin)
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getChatRoomsForUser(@PathVariable Long userId) {
        try {
            List<ChatRoomDTO> chatRooms = chatRoomService.getUserChatRoomsDTO(userId);
            return ResponseEntity.ok(chatRooms);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi lấy danh sách phòng chat: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // Lấy danh sách thành viên của phòng chat
    @GetMapping("/{chatRoomId}/members")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getChatRoomMembers(@PathVariable Long chatRoomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            // Kiểm tra quyền truy cập
            if (!chatRoomService.isUserMemberOfRoom(chatRoomId, userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Bạn không có quyền xem danh sách thành viên"));
            }

            List<ChatMemberDTO> members = chatRoomService.getChatRoomMembers(chatRoomId);
            return ResponseEntity.ok(members);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi lấy danh sách thành viên: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // Lấy tin nhắn của phòng chat
    @GetMapping("/{chatRoomId}/messages")
    @PreAuthorize("hasRole('USER') ")
    public ResponseEntity<?> getMessages(@PathVariable Long chatRoomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            List<ChatMessageDTO> messages = chatRoomService.getMessages(chatRoomId, userDetails.getId());
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi lấy tin nhắn: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // Rời khỏi phòng chat
    @PostMapping("/{chatRoomId}/leave")
    @PreAuthorize("hasRole('USER') ")
    public ResponseEntity<?> leaveChatRoom(@PathVariable Long chatRoomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            chatRoomService.leaveChatRoom(chatRoomId, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Rời khỏi phòng chat thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi rời khỏi phòng chat: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // Xóa phòng chat (chỉ người tạo nhóm)
    @DeleteMapping("/{chatRoomId}")
    @PreAuthorize("hasRole('USER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteChatRoom(@PathVariable Long chatRoomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            chatRoomService.deleteChatRoom(chatRoomId, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Xóa phòng chat thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi xóa phòng chat: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // Xóa tất cả tin nhắn trong phòng chat (chỉ người tạo nhóm)
    @DeleteMapping("/{chatRoomId}/messages")
    @PreAuthorize("hasRole('USER') ")
    public ResponseEntity<?> clearChatHistory(@PathVariable Long chatRoomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            chatRoomService.clearChatHistory(chatRoomId, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Xóa lịch sử tin nhắn thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // Xóa tin nhắn riêng lẻ (người gửi hoặc người tạo nhóm)
    @DeleteMapping("/{chatRoomId}/messages/{messageId}")
    @PreAuthorize("hasRole('USER') ")
    public ResponseEntity<?> deleteMessage(@PathVariable Long chatRoomId, @PathVariable Long messageId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            chatRoomService.deleteMessage(chatRoomId, messageId, userDetails.getId());
            return ResponseEntity.ok(new MessageResponse("Xóa tin nhắn thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi xóa tin nhắn: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    // HTTP POST endpoint để gửi tin nhắn
    @PostMapping("/{chatRoomId}/messages")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> sendMessage(@PathVariable Long chatRoomId, @RequestBody ChatMessageDTO message) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            ChatMessage savedMessage = chatRoomService.sendMessage(chatRoomId, userDetails.getId(), message.getContent());
            
            // Tạo ChatMessageDTO để trả về
            ChatMessageDTO responseDTO = new ChatMessageDTO(
                savedMessage.getId(),
                savedMessage.getUser().getId(),
                savedMessage.getUser().getFullName(),
                savedMessage.getContent(),
                savedMessage.getSentAt()
            );
            
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Lỗi gửi tin nhắn: " + e.getMessage()));
        }
    }

    // WebSocket mapping để gửi tin nhắn
    @MessageMapping("/chat/{chatRoomId}")
    public void sendMessage(@Payload ChatMessageDTO message, @DestinationVariable Long chatRoomId, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            chatRoomService.sendMessage(chatRoomId, userDetails.getId(), message.getContent());
        } catch (RuntimeException e) {
            // Log error hoặc gửi thông báo lỗi qua WebSocket
            System.err.println("Lỗi gửi tin nhắn: " + e.getMessage());
        }
    }

    // Request DTOs
    static class CreateChatRoomRequest {
        private String name;
        private Long creatorUserId;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getCreatorUserId() { return creatorUserId; }
        public void setCreatorUserId(Long creatorUserId) { this.creatorUserId = creatorUserId; }
    }

    static class AddMemberRequest {
        private String phoneNumber;

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }

    static class AddMemberByEmailRequest {
        private String email;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // Response DTO
    static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}