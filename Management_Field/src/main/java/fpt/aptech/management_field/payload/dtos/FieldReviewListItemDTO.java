package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldReviewListItemDTO {
    private Long id;
    private Long fieldId;
    private String fieldName;
    private Long locationId;
    private String locationName;

    private Long userId;
    private String userName;

    private BigDecimal rating;
    private String content;
    private LocalDateTime createdAt;

    // Phần reply của owner
    private String ownerReply;
    private LocalDateTime ownerRepliedAt;


}
