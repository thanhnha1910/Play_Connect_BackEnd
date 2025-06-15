package fpt.aptech.management_field.payload.response;

import fpt.aptech.management_field.payload.dtos.FieldTypeDTO;
import fpt.aptech.management_field.payload.dtos.LocationReviewDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDetailResponse {
    private String name;
    private String address;
    private List<FieldTypeDTO> fieldTypes;
    private List<LocationReviewDTO> reviews;
}
