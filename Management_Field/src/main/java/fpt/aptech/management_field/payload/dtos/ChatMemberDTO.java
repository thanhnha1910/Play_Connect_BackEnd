package fpt.aptech.management_field.payload.dtos;

import java.time.LocalDateTime;

public class ChatMemberDTO {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private boolean isAdmin;
    private boolean isCreator;
    private boolean isActive;
    private LocalDateTime joinedAt;

    public ChatMemberDTO() {}

    public ChatMemberDTO(Long id, Long userId, String username, boolean isAdmin, boolean isCreator, boolean isActive, LocalDateTime joinedAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.isAdmin = isAdmin;
        this.isCreator = isCreator;
        this.isActive = isActive;
        this.joinedAt = joinedAt;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public boolean isCreator() { return isCreator; }
    public void setCreator(boolean creator) { isCreator = creator; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}