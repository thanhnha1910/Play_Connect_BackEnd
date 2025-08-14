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
public class DashboardWidgetDto {
    private List<StatCard> statCards;
    private List<ChartData> chartData;
    private List<RecentActivity> recentActivities;
    private SystemHealth systemHealth;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StatCard {
        private String title;
        private String value;
        private String description;
        private String icon;
        private String change;
        private String trend; // "up", "down", "stable"
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ChartData {
        private String type; // "line", "bar", "pie", "area"
        private String title;
        private List<DataPoint> data;
        private String xAxisLabel;
        private String yAxisLabel;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataPoint {
        private String label;
        private double value;
        private String color;
        private Object metadata;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RecentActivity {
        private String id;
        private String user;
        private String action;
        private String description;
        private String timestamp;
        private String status;
        private String icon;
        private String link;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SystemHealth {
        private double uptime;
        private int responseTime;
        private double errorRate;
        private String databaseStatus;
        private String paymentGatewayStatus;
        private String emailServiceStatus;
        private String fileStorageStatus;
    }
}