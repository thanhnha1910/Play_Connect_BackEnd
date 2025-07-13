package fpt.aptech.management_field.payload.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageResponseDTO {
    private Long id; // optional message ID
    private String content;
    private LocalDateTime timestamp;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private boolean isRead; // <-- added field
}
