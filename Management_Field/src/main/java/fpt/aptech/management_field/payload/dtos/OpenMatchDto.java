package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenMatchDto {
    private Long id;
    private Long bookingId;
    private Long creatorUserId;
    private String creatorUserName;
    private String creatorAvatarUrl;
    private String sportType;
    private Integer slotsNeeded;
    private List<String> requiredTags;
    private String status;
    private LocalDateTime createdAt;
    private String fieldName;
    private String locationAddress;
    private Instant startTime;
    private Instant endTime;
    private LocalDate bookingDate;
    private Integer currentParticipants;
    private String locationName;
    private Double compatibilityScore; // For AI ranking
}