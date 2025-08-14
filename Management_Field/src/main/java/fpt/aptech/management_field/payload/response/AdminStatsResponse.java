package fpt.aptech.management_field.payload.response;

public class AdminStatsResponse {
    private long totalUsers;
    private long activeOwners;
    private long pendingOwners;
    private long suspendedUsers;
    private long totalFields;
    private long recentBookings;

    public AdminStatsResponse() {}

    public AdminStatsResponse(long totalUsers, long activeOwners, long pendingOwners, 
                             long suspendedUsers, long totalFields, long recentBookings) {
        this.totalUsers = totalUsers;
        this.activeOwners = activeOwners;
        this.pendingOwners = pendingOwners;
        this.suspendedUsers = suspendedUsers;
        this.totalFields = totalFields;
        this.recentBookings = recentBookings;
    }

    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveOwners() {
        return activeOwners;
    }

    public void setActiveOwners(long activeOwners) {
        this.activeOwners = activeOwners;
    }

    public long getPendingOwners() {
        return pendingOwners;
    }

    public void setPendingOwners(long pendingOwners) {
        this.pendingOwners = pendingOwners;
    }

    public long getSuspendedUsers() {
        return suspendedUsers;
    }

    public void setSuspendedUsers(long suspendedUsers) {
        this.suspendedUsers = suspendedUsers;
    }

    public long getTotalFields() {
        return totalFields;
    }

    public void setTotalFields(long totalFields) {
        this.totalFields = totalFields;
    }

    public long getRecentBookings() {
        return recentBookings;
    }

    public void setRecentBookings(long recentBookings) {
        this.recentBookings = recentBookings;
    }
      private long totalCommission; // Admin's 5% commission
    private long totalRevenue;    // Total booking amounts
    private double averageBookingValue;
    private double commissionRate = 0.05;
}