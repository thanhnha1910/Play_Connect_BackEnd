package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingHistoryDto {
    private Long bookingId;
    private String fieldName;
    private String fieldAddress;
    private String coverImageUrl;
    private Instant startTime;
    private Instant endTime;
    private Double totalPrice;
    private String status;
}