package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerSummaryDto {
    private Long id;
    private String fullName;
    private String email;
    private String status;
    private Instant registrationDate;
    private String businessName;
    
    public OwnerSummaryDto(Long id, String fullName, String email, String status, Instant registrationDate) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
        this.registrationDate = registrationDate;
    }
}