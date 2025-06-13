package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldSummaryResponse {
    private Long fieldId;
    private String fieldName;
    private Integer hourlyRate;
    private String typeName;
    private String categoryName;
    private String thumbnailImageUrl;
}