package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldSummaryDto {
    private Long fieldId;
    private String name;
    private String fieldTypeName;
    private String locationName;
    private Boolean isActive;
    private Integer hourlyRate;
}