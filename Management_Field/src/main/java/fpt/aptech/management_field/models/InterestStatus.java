package fpt.aptech.management_field.models;

/**
 * Enum representing the status of a user's interest in a Draft Match
 */
public enum InterestStatus {
    /**
     * User has expressed interest and is waiting for creator's response
     */
    PENDING("Chờ phản hồi", "User has expressed interest and is waiting for response"),
    
    /**
     * User has been accepted by the draft match creator
     */
    ACCEPTED("Đã chấp nhận", "User has been accepted by the creator"),
    
    /**
     * User has been rejected by the draft match creator
     */
    REJECTED("Đã từ chối", "User has been rejected by the creator"),
    
    /**
     * User has withdrawn their interest
     */
    WITHDRAWN("Đã rút quan tâm", "User has withdrawn their interest");
    
    private final String displayName;
    private final String description;
    
    InterestStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if the status allows the user to withdraw interest
     */
    public boolean canWithdraw() {
        return this == PENDING;
    }
    
    /**
     * Check if the status allows the creator to accept the user
     */
    public boolean canAccept() {
        return this == PENDING;
    }
    
    /**
     * Check if the status allows the creator to reject the user
     */
    public boolean canReject() {
        return this == PENDING || this == ACCEPTED;
    }
    
    /**
     * Check if the status is final (no further actions possible)
     */
    public boolean isFinal() {
        return this == REJECTED || this == WITHDRAWN;
    }
    
    /**
     * Check if the status is active (user is still in consideration)
     */
    public boolean isActive() {
        return this == PENDING || this == ACCEPTED;
    }
    
    /**
     * Check if the user is confirmed for the draft match
     */
    public boolean isConfirmed() {
        return this == ACCEPTED;
    }
    
    /**
     * Get status from string value (case insensitive)
     */
    public static InterestStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return PENDING; // Default status
        }
        
        try {
            return InterestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING; // Default status if invalid
        }
    }
    
    /**
     * Get all active statuses (statuses that keep user in consideration)
     */
    public static InterestStatus[] getActiveStatuses() {
        return new InterestStatus[]{PENDING, ACCEPTED};
    }
    
    /**
     * Get all final statuses (statuses that end user's participation)
     */
    public static InterestStatus[] getFinalStatuses() {
        return new InterestStatus[]{REJECTED, WITHDRAWN};
    }
    
    /**
     * Get CSS class for UI styling based on status
     */
    public String getCssClass() {
        switch (this) {
            case PENDING:
                return "status-pending";
            case ACCEPTED:
                return "status-accepted";
            case REJECTED:
                return "status-rejected";
            case WITHDRAWN:
                return "status-withdrawn";
            default:
                return "status-unknown";
        }
    }
    
    /**
     * Get color code for UI display
     */
    public String getColorCode() {
        switch (this) {
            case PENDING:
                return "#FFA500"; // Orange
            case ACCEPTED:
                return "#28A745"; // Green
            case REJECTED:
                return "#DC3545"; // Red
            case WITHDRAWN:
                return "#6C757D"; // Gray
            default:
                return "#000000"; // Black
        }
    }
    
    /**
     * Get icon name for UI display (using common icon libraries)
     */
    public String getIconName() {
        switch (this) {
            case PENDING:
                return "clock";
            case ACCEPTED:
                return "check-circle";
            case REJECTED:
                return "x-circle";
            case WITHDRAWN:
                return "minus-circle";
            default:
                return "help-circle";
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}