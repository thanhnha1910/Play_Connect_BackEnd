package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDto {
    private Long userId;
    private String fullName;
    private String imageUrl;
    private String email;
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime requestedAt;
    private LocalDateTime updatedAt;
    private Double compatibilityScore; // AI compatibility score
    
    // Explicit setters for compatibility
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setCompatibilityScore(Double compatibilityScore) {
        this.compatibilityScore = compatibilityScore;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}