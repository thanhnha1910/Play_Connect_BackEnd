package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldMapResponse {
    private Long fieldId;
    private String fieldName;
    private String locationName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String thumbnailImageUrl;
    private String addressShort;
    private BigDecimal averageRating;
    private Integer hourlyRate;
    private Long typeId;
    private String typeName;
    private Long categoryId;
    private String categoryName;
}