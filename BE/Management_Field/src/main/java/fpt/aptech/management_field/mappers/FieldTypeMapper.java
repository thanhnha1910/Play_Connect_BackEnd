package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.FieldType;
import fpt.aptech.management_field.payload.dtos.FieldTypeDTO;

public class FieldTypeMapper {
    public static FieldTypeDTO mapToDTO(FieldType fieldType) {
        FieldTypeDTO fieldTypeDTO = new FieldTypeDTO();
        fieldTypeDTO.setName(fieldType.getName());
        fieldTypeDTO.setTeamCapacity(fieldType.getTeamCapacity());
        fieldTypeDTO.setMaxCapacity(fieldType.getMaxCapacity());
        return fieldTypeDTO;
    }
}
