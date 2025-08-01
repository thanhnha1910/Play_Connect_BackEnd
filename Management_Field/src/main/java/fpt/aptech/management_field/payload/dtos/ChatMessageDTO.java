package fpt.aptech.management_field.payload.dtos;

import java.time.LocalDateTime;

public class ChatMessageDTO {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private LocalDateTime sentAt;

    public ChatMessageDTO() {}

    public ChatMessageDTO(Long id, Long userId, String username, String content, LocalDateTime sentAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.sentAt = sentAt;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}