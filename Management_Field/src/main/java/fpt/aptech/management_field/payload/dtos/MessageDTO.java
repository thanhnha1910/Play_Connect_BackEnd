package fpt.aptech.management_field.payload.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;  // <-- New field added
}
