// Admin Analytics Service - Business logic for dashboard and analytics
package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.*;
import fpt.aptech.management_field.payload.dtos.*;
import fpt.aptech.management_field.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminAnalyticsService {

    @Autowired
    private AdminRevenueRepository adminRevenueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public DashboardWidgetDto getDashboardWidgets() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime sixtyDaysAgo = now.minusDays(60);

        // Get current period stats
        Long currentCommission = adminRevenueRepository.getTotalCommissionByDateRange(thirtyDaysAgo, now);
        Long currentRevenue = adminRevenueRepository.getTotalRevenueByDateRange(thirtyDaysAgo, now);
        Long currentBookings = adminRevenueRepository.getBookingCountByDateRange(thirtyDaysAgo, now);

        // Get previous period for comparison
        Long previousCommission = adminRevenueRepository.getTotalCommissionByDateRange(sixtyDaysAgo, thirtyDaysAgo);
        Long previousRevenue = adminRevenueRepository.getTotalRevenueByDateRange(sixtyDaysAgo, thirtyDaysAgo);
        Long previousBookings = adminRevenueRepository.getBookingCountByDateRange(sixtyDaysAgo, thirtyDaysAgo);

        // Calculate changes
        String commissionChange = calculatePercentageChange(previousCommission, currentCommission);
        String revenueChange = calculatePercentageChange(previousRevenue, currentRevenue);
        String bookingsChange = calculatePercentageChange(previousBookings, currentBookings);

        // Build stat cards
        List<DashboardWidgetDto.StatCard> statCards = Arrays.asList(
                DashboardWidgetDto.StatCard.builder()
                        .title("Admin Commission (30 days)")
                        .value("$" + (currentCommission != null ? currentCommission : 0))
                        .description("5% commission from bookings")
                        .icon("DollarSign")
                        .change(commissionChange)
                        .trend(getTrend(commissionChange))
                        .color("green")
                        .build(),

                DashboardWidgetDto.StatCard.builder()
                        .title("Total Revenue (30 days)")
                        .value("$" + (currentRevenue != null ? currentRevenue : 0))
                        .description("Total booking revenue")
                        .icon("TrendingUp")
                        .change(revenueChange)
                        .trend(getTrend(revenueChange))
                        .color("blue")
                        .build(),

                DashboardWidgetDto.StatCard.builder()
                        .title("Total Bookings (30 days)")
                        .value(String.valueOf(currentBookings != null ? currentBookings : 0))
                        .description("Confirmed bookings")
                        .icon("Calendar")
                        .change(bookingsChange)
                        .trend(getTrend(bookingsChange))
                        .color("purple")
                        .build(),

                DashboardWidgetDto.StatCard.builder()
                        .title("Average Booking Value")
                        .value("$" + (currentBookings != null && currentBookings > 0 ?
                                (currentRevenue != null ? currentRevenue / currentBookings : 0) : 0))
                        .description("Average per booking")
                        .icon("BarChart3")
                        .change("0%")
                        .trend("stable")
                        .color("orange")
                        .build()
        );

        // Generate chart data
        List<DashboardWidgetDto.ChartData> chartData = generateChartData(thirtyDaysAgo, now);

        // Get recent activities
        List<DashboardWidgetDto.RecentActivity> recentActivities = getRecentActivities(10);

        // Get system health
        DashboardWidgetDto.SystemHealth systemHealth = getSystemHealth();

        return DashboardWidgetDto.builder()
                .statCards(statCards)
                .chartData(chartData)
                .recentActivities(recentActivities)
                .systemHealth(systemHealth)
                .build();
    }

    public List<AdminAnalyticsDto.DailyRevenueData> getDailyRevenueData(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> rawData = adminRevenueRepository.getDailyRevenueData(start, end);

        return rawData.stream()
                .map(row -> {
                    String date = row[0].toString();
                    long commission = ((Number) row[1]).longValue();
                    long revenue = ((Number) row[2]).longValue();
                    long bookings = ((Number) row[3]).longValue();
                    double averageValue = bookings > 0 ? (double) revenue / bookings : 0;

                    return new AdminAnalyticsDto.DailyRevenueData(date, commission, revenue, bookings, averageValue);
                })
                .collect(Collectors.toList());
    }

    public List<AdminAnalyticsDto.TopField> getTopPerformingFields(LocalDate startDate, LocalDate endDate, int limit) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> rawData = adminRevenueRepository.getTopPerformingFields(start, end);

        return rawData.stream()
                .limit(limit)
                .map(row -> {
                    String name = (String) row[0];
                    long bookings = ((Number) row[1]).longValue();
                    long commission = ((Number) row[2]).longValue();
                    long revenue = commission > 0 ? (long) (commission / 0.05) : 0; // Calculate total revenue from commission

                    return new AdminAnalyticsDto.TopField(name, bookings, commission, revenue, "");
                })
                .collect(Collectors.toList());
    }

    public List<AdminAnalyticsDto.HourlyData> getHourlyBookingData(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> rawData = adminRevenueRepository.getHourlyBookingData(start, end);

        return rawData.stream()
                .map(row -> {
                    int hour = ((Number) row[0]).intValue();
                    long bookings = ((Number) row[1]).longValue();
                    long revenue = bookings * 50; // Estimated revenue
                    String timeLabel = String.format("%02d:00", hour);

                    return new AdminAnalyticsDto.HourlyData(hour, bookings, revenue, timeLabel);
                })
                .collect(Collectors.toList());
    }

    public List<AdminAnalyticsDto.LocationRevenue> getLocationPerformance(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> rawData = adminRevenueRepository.getRevenueByLocation(start, end);

        return rawData.stream()
                .map(row -> {
                    String locationName = (String) row[0];
                    long bookings = ((Number) row[1]).longValue();
                    long commission = ((Number) row[2]).longValue();
                    long revenue = commission > 0 ? (long) (commission / 0.05) : 0;

                    return new AdminAnalyticsDto.LocationRevenue(locationName, bookings, commission, revenue, 0);
                })
                .collect(Collectors.toList());
    }

    public AdminAnalyticsDto.BookingStatusStats getBookingStatusStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getCreatedAt() != null &&
                        !booking.getCreatedAt().isBefore(start) &&
                        booking.getCreatedAt().isBefore(end))
                .collect(Collectors.toList());

        long confirmed = bookings.stream().filter(b -> "confirmed".equals(b.getStatus())).count();
        long pending = bookings.stream().filter(b -> "pending".equals(b.getStatus())).count();
        long cancelled = bookings.stream().filter(b -> "cancelled".equals(b.getStatus())).count();
        long completed = bookings.stream().filter(b -> "completed".equals(b.getStatus())).count();

        long total = bookings.size();
        double confirmationRate = total > 0 ? (double) confirmed / total * 100 : 0;
        double cancellationRate = total > 0 ? (double) cancelled / total * 100 : 0;

        return new AdminAnalyticsDto.BookingStatusStats(
                confirmed, pending, cancelled, completed, confirmationRate, cancellationRate
        );
    }

    public RevenueAnalyticsDto getRevenueAnalytics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        // Overview
        Long totalRevenue = adminRevenueRepository.getTotalRevenueByDateRange(start, end);
        Long totalCommission = adminRevenueRepository.getTotalCommissionByDateRange(start, end);
        Long totalBookings = adminRevenueRepository.getBookingCountByDateRange(start, end);

        RevenueAnalyticsDto.RevenueOverview overview = new RevenueAnalyticsDto.RevenueOverview(
                totalRevenue != null ? totalRevenue : 0,
                totalCommission != null ? totalCommission : 0,
                totalBookings != null ? totalBookings : 0,
                totalBookings != null && totalBookings > 0 ? (double) totalRevenue / totalBookings : 0,
                0.05,
                LocalDateTime.now()
        );

        // Monthly data (simplified)
        List<RevenueAnalyticsDto.MonthlyRevenue> monthlyData = generateMonthlyData(startDate, endDate);

        return RevenueAnalyticsDto.builder()
                .overview(overview)
                .monthlyData(monthlyData)
                .categoryBreakdown(new ArrayList<>())
                .topOwners(new ArrayList<>())
                .growth(new RevenueAnalyticsDto.RevenueGrowth(5.2, 15.8, "up", "Positive growth expected"))
                .build();
    }

    public List<DashboardWidgetDto.RecentActivity> getRecentActivities(int limit) {
        List<AdminRevenue> recentRevenues = adminRevenueRepository.findTop10ByOrderByCreatedAtDesc();

        return recentRevenues.stream()
                .limit(limit)
                .map(revenue -> DashboardWidgetDto.RecentActivity.builder()
                        .id(revenue.getId().toString())
                        .user("User #" + revenue.getBookingId())
                        .action("Booking Payment")
                        .description(String.format("Payment for %s - $%d (Commission: $%d)",
                                revenue.getFieldName(), revenue.getBookingAmount(), revenue.getCommissionAmount()))
                        .timestamp(formatRelativeTime(revenue.getCreatedAt()))
                        .status("success")
                        .icon("DollarSign")
                        .link("/admin/transactions/" + revenue.getId())
                        .build())
                .collect(Collectors.toList());
    }

    public List<AdminAnalyticsDto.RecentTransaction> getRecentTransactions(int limit) {
        List<AdminRevenue> recentRevenues = adminRevenueRepository.findTop10ByOrderByCreatedAtDesc();

        return recentRevenues.stream()
                .limit(limit)
                .map(revenue -> new AdminAnalyticsDto.RecentTransaction(
                        revenue.getId(),
                        revenue.getFieldName(),
                        "User #" + revenue.getBookingId(),
                        revenue.getLocationName(),
                        revenue.getBookingAmount().longValue(),
                        revenue.getCommissionAmount().longValue(),
                        "SUCCESS",
                        revenue.getCreatedAt().toString(),
                        revenue.getBookingDate().toString()
                ))
                .collect(Collectors.toList());
    }

    public Page<AdminRevenue> getTransactions(Pageable pageable, LocalDate startDate, LocalDate endDate, String search) {
        Specification<AdminRevenue> spec = Specification.where(null);

        if (startDate != null && endDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(23, 59, 59);
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("createdAt"), start, end));
        }

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("fieldName")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("locationName")), "%" + search.toLowerCase() + "%")
                    ));
        }

        return adminRevenueRepository.findAll(spec, pageable);
    }

    public DashboardWidgetDto.SystemHealth getSystemHealth() {
        return DashboardWidgetDto.SystemHealth.builder()
                .uptime(99.9)
                .responseTime(145)
                .errorRate(0.1)
                .databaseStatus("Healthy")
                .paymentGatewayStatus("Online")
                .emailServiceStatus("Active")
                .fileStorageStatus("Warning")
                .build();
    }

    public Map<String, Object> getSystemPerformance() {
        Map<String, Object> performance = new HashMap<>();
        performance.put("cpuUsage", 45.2);
        performance.put("memoryUsage", 67.8);
        performance.put("diskUsage", 34.5);
        performance.put("networkLatency", 12.3);
        performance.put("activeConnections", 156);
        performance.put("cacheHitRate", 89.7);
        return performance;
    }

    public Map<String, Object> exportTransactions(LocalDate startDate, LocalDate endDate, String format) {
        List<AdminRevenue> transactions = adminRevenueRepository.findAll().stream()
                .filter(t -> {
                    LocalDate transactionDate = t.getCreatedAt().toLocalDate();
                    return !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("format", format);
        result.put("totalRecords", transactions.size());
        result.put("data", transactions);
        result.put("filename", String.format("transactions_%s_to_%s.%s", startDate, endDate, format));

        return result;
    }

    // Helper methods
    private String calculatePercentageChange(Long previous, Long current) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? "+100%" : "0%";
        }
        if (current == null) current = 0L;

        double change = ((double) (current - previous) / previous) * 100;
        return String.format("%+.1f%%", change);
    }

    private String getTrend(String change) {
        if (change.startsWith("+")) return "up";
        if (change.startsWith("-")) return "down";
        return "stable";
    }

    private List<DashboardWidgetDto.ChartData> generateChartData(LocalDateTime start, LocalDateTime end) {
        List<DashboardWidgetDto.ChartData> charts = new ArrayList<>();

        // Revenue trend chart
        List<Object[]> dailyData = adminRevenueRepository.getDailyRevenueData(start, end);
        List<DashboardWidgetDto.DataPoint> revenuePoints = dailyData.stream()
                .map(row -> new DashboardWidgetDto.DataPoint(
                        row[0].toString(),
                        ((Number) row[2]).doubleValue(),
                        "#8884d8",
                        null
                ))
                .collect(Collectors.toList());

        charts.add(DashboardWidgetDto.ChartData.builder()
                .type("line")
                .title("Revenue Trend")
                .data(revenuePoints)
                .xAxisLabel("Date")
                .yAxisLabel("Revenue ($)")
                .build());

        return charts;
    }

    private List<RevenueAnalyticsDto.MonthlyRevenue> generateMonthlyData(LocalDate startDate, LocalDate endDate) {
        // Simplified monthly data generation
        List<RevenueAnalyticsDto.MonthlyRevenue> monthlyData = new ArrayList<>();

        LocalDate current = startDate.withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            LocalDateTime monthStart = current.atStartOfDay();
            LocalDateTime monthEnd = current.plusMonths(1).minusDays(1).atTime(23, 59, 59);

            Long monthRevenue = adminRevenueRepository.getTotalRevenueByDateRange(monthStart, monthEnd);
            Long monthCommission = adminRevenueRepository.getTotalCommissionByDateRange(monthStart, monthEnd);
            Long monthBookings = adminRevenueRepository.getBookingCountByDateRange(monthStart, monthEnd);

            monthlyData.add(new RevenueAnalyticsDto.MonthlyRevenue(
                    current.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + current.getYear(),
                    monthRevenue != null ? monthRevenue : 0,
                    monthCommission != null ? monthCommission : 0,
                    monthBookings != null ? monthBookings : 0,
                    5.0 // Placeholder growth
            ));

            current = current.plusMonths(1);
        }

        return monthlyData;
    }

    private String formatRelativeTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " minutes ago";
        if (minutes < 1440) return (minutes / 60) + " hours ago";
        return (minutes / 1440) + " days ago";
    }
}