package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailedAnalyticsDto {
    private List<TimeSeriesData> userGrowth;
    private List<TimeSeriesData> revenueTrend;
    private List<TopPerformingItem> topFields;
    private BookingStats bookingStats;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesData {
        private String date;
        private Long value;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPerformingItem {
        private String name;
        private Long bookings;
        private Double revenue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingStats {
        private Long totalBookings;
        private Double totalRevenue;
        private Double averageBookingValue;
        private List<HourlyBookingData> peakHours;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyBookingData {
        private Integer hour;
        private Long bookings;
    }
}