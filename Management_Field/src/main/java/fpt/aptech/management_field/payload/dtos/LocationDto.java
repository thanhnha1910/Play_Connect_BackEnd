package fpt.aptech.management_field.payload.dtos;

import lombok.Data;

@Data
public class LocationDto {
    private Long locationId;
    private String name;
    private String address;
    private String city;
    private String country;
}
