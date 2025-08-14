package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAnalyticsDto {
    private List<DailyRevenueData> dailyRevenueData;
    private List<TopField> topFields;
    private List<HourlyData> hourlyBookings;
    private List<LocationRevenue> locationRevenues;
    private List<UserGrowthData> userGrowthData;
    private List<RecentTransaction> recentTransactions;
    private BookingStatusStats bookingStatusStats;

    // Summary statistics
    private long totalCommission;
    private long totalRevenue;
    private long totalBookings;
    private long averageBookingValue;
    private double growthRate;
    private int peakHour;
    private String topLocation;
    private String topField;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyRevenueData {
        private String date;
        private long commission;
        private long revenue;
        private long bookings;
        private double averageValue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopField {
        private String name;
        private long bookings;
        private long commission;
        private long revenue;
        private String location;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HourlyData {
        private int hour;
        private long bookings;
        private long revenue;
        private String timeLabel; // e.g., "09:00 AM"
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationRevenue {
        private String locationName;
        private long bookings;
        private long commission;
        private long revenue;
        private int fieldsCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserGrowthData {
        private String date;
        private long newUsers;
        private long totalUsers;
        private long activeUsers;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentTransaction {
        private Long transactionId;
        private String fieldName;
        private String userName;
        private String locationName;
        private long amount;
        private long commission;
        private String status;
        private String createdAt;
        private String bookingDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BookingStatusStats {
        private long confirmed;
        private long pending;
        private long cancelled;
        private long completed;
        private double confirmationRate;
        private double cancellationRate;
    }
}