package fpt.aptech.management_field.payload.dtos;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatar;
}
