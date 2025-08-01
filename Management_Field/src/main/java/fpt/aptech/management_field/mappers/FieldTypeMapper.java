package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.FieldType;
import fpt.aptech.management_field.payload.dtos.FieldTypeDto;

public class FieldTypeMapper {
    public static FieldTypeDto mapToDTO(FieldType fieldType) {
        FieldTypeDto fieldTypeDTO = new FieldTypeDto();
        fieldTypeDTO.setName(fieldType.getName());
        fieldTypeDTO.setTeamCapacity(fieldType.getTeamCapacity());
        fieldTypeDTO.setMaxCapacity(fieldType.getMaxCapacity());
        return fieldTypeDTO;
    }
}
