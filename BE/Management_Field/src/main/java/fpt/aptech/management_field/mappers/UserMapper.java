package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.UserDTO;

public class UserMapper {
    public static UserDTO mapToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        user.setProfilePicture(user.getProfilePicture());
        return userDTO;
    }
}
