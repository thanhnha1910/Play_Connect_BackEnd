package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldDTO {
    private Long id;
    private String name;
    private String description;
    private Integer hourlyRate;
    private List<BookingDTO> bookings;
    
    // Constructor without bookings for backward compatibility
    public FieldDTO(Long id, String name, String description, Integer hourlyRate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.hourlyRate = hourlyRate;
    }
}
