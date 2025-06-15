package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldDTO {
    private Long id;
    private String name;
    private String description;
    private Integer hourlyRate;
}
