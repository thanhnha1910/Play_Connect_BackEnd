package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {
    private Instant fromTime;
    private Instant toTime;
    private Integer slots;
    private String status;
    private String customerName;
    private String customerPhone;
    private boolean isBooked;
}
