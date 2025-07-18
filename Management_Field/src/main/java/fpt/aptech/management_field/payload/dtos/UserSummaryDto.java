package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private Long id;
    private String fullName;
    private String email;
    private String avatarUrl;
    private LocalDateTime joinDate;
    private String status;
}