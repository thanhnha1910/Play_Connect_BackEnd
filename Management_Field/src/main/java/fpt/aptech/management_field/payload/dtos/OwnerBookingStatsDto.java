package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for owner booking statistics response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerBookingStatsDto {
    private long totalBookings;
    private long upcomingCount;
    private long pendingCount;
    private double thisMonthRevenue;
}