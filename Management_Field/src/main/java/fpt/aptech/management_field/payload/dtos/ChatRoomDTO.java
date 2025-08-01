package fpt.aptech.management_field.payload.dtos;

import java.time.LocalDateTime;

public class ChatRoomDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int memberCount;

    public ChatRoomDTO() {}

    public ChatRoomDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public ChatRoomDTO(Long id, String name, LocalDateTime createdAt, String lastMessage,
                       LocalDateTime lastMessageTime, int memberCount) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.memberCount = memberCount;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
}