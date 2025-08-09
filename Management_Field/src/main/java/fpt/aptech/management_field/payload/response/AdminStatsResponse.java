package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long activeOwners;
    private long pendingOwners;
    private long suspendedUsers;
    private long totalFields;
    private long recentBookings;
    private long totalCommission; // Admin's 5% commission
    private long totalRevenue;    // Total booking amounts
    private double averageBookingValue;
    private double commissionRate = 0.05;
}
