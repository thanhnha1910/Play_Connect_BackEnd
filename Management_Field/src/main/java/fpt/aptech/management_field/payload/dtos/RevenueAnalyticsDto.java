package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueAnalyticsDto {
    private RevenueOverview overview;
    private List<MonthlyRevenue> monthlyData;
    private List<CategoryRevenue> categoryBreakdown;
    private List<TopOwner> topOwners;
    private RevenueGrowth growth;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RevenueOverview {
        private long totalRevenue;
        private long totalCommission;
        private long totalBookings;
        private double averageBookingValue;
        private double commissionRate;
        private LocalDateTime lastUpdated;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthlyRevenue {
        private String month;
        private long revenue;
        private long commission;
        private long bookings;
        private double growth;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryRevenue {
        private String category; // Sport type or field type
        private long revenue;
        private long commission;
        private long bookings;
        private double percentage;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopOwner {
        private String ownerName;
        private String businessName;
        private long totalBookings;
        private long totalRevenue;
        private long adminCommission;
        private int fieldsCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RevenueGrowth {
        private double monthlyGrowthRate;
        private double yearlyGrowthRate;
        private String trendDirection; // "up", "down", "stable"
        private String prediction; // Simple prediction text
    }
}