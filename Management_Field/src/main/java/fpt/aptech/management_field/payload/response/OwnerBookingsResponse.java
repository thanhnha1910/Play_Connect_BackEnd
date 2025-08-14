package fpt.aptech.management_field.payload.response;

import fpt.aptech.management_field.payload.dtos.OwnerBookingDto;
import fpt.aptech.management_field.payload.dtos.PaginationDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for owner bookings with pagination
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerBookingsResponse {
    private List<OwnerBookingDto> data;
    private PaginationDto pagination;
}