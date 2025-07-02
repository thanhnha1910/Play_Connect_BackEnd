package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldTypeDTO {
    private String name;
    private Integer teamCapacity;
    private Integer maxCapacity;
    private List<FieldDTO> fields;
}
