package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldDetailDto {
    private Long fieldId;
    private String name;
    private String description;
    private String fieldTypeName;
    private String locationName;
    private Boolean isActive;
    private Integer hourlyRate;
    private Long typeId;
    private Long locationId;
}