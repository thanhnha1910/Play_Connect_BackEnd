package fpt.aptech.management_field.models;

/**
 * Enum representing the status of a Draft Match
 */
public enum DraftMatchStatus {
    RECRUITING,     // Draft match is actively recruiting players
    FULL,          // Draft match has enough players but not yet converted
    AWAITING_CONFIRMATION, // Waiting for final confirmation before booking
    CONVERTED,     // Draft match has been converted to a real match
    CANCELLED,     // Draft match has been cancelled
    EXPIRED        // Draft match has expired without being converted
}