package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingOwnerDto {
    private Long id;
    private String fullName;
    private String email;
    private String businessName;
    private LocalDateTime registrationDate;
    private String status;
    
    public PendingOwnerDto(Long id, String fullName, String email, String businessName, String status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.businessName = businessName;
        this.status = status;
        this.registrationDate = LocalDateTime.now(); // Default to current time if not provided
    }
}