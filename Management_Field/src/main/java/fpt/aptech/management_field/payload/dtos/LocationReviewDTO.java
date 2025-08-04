package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationReviewDTO {
    private BigDecimal rating;
    private String comment;
    private UserDto user; // Sửa từ UserDTO thành UserDto
    
}
