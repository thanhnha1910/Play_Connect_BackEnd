package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.*;
import fpt.aptech.management_field.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
public class TimeConflictValidationService {

    @Autowired
    private DraftMatchRepository draftMatchRepository;
    
    @Autowired
    private OpenMatchRepository openMatchRepository;
    
    @Autowired
    private OpenMatchParticipantRepository openMatchParticipantRepository;
    
    @Autowired
    private DraftMatchUserStatusRepository draftMatchUserStatusRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private BookingUserRepository bookingUserRepository;

    /**
     * Check time conflicts for a user with given time range
     */
    public ValidationResult checkTimeConflictsForUser(Long userId, LocalDateTime startTime, LocalDateTime endTime, String location) {
        List<ConflictInfo> conflicts = new ArrayList<>();
        
        // Check draft match conflicts
        List<DraftMatch> userDraftMatches = getUserActiveDraftMatches(userId);
        for (DraftMatch match : userDraftMatches) {
            if (hasTimeOverlap(startTime, endTime, match.getEstimatedStartTime(), match.getEstimatedEndTime())) {
                conflicts.add(new ConflictInfo(
                    "DRAFT_MATCH", 
                    match.getId(), 
                    match.getSportType(),
                    match.getLocationDescription(),
                    match.getEstimatedStartTime(),
                    match.getEstimatedEndTime(),
                    calculateConflictSeverity(startTime, endTime, match.getEstimatedStartTime(), match.getEstimatedEndTime())
                ));
            }
        }
        
        // Check open match conflicts
        List<OpenMatch> userOpenMatches = getUserActiveOpenMatches(userId);
        for (OpenMatch match : userOpenMatches) {
            Booking booking = match.getBooking();
            LocalDateTime matchStart = LocalDateTime.ofInstant(booking.getFromTime(), ZoneId.systemDefault());
            LocalDateTime matchEnd = LocalDateTime.ofInstant(booking.getToTime(), ZoneId.systemDefault());
            
            if (hasTimeOverlap(startTime, endTime, matchStart, matchEnd)) {
                conflicts.add(new ConflictInfo(
                    "OPEN_MATCH", 
                    match.getId(), 
                    match.getSportType(),
                    booking.getField().getLocation().getName(),
                    matchStart,
                    matchEnd,
                    calculateConflictSeverity(startTime, endTime, matchStart, matchEnd)
                ));
            }
        }
        
        // Check personal booking conflicts
        List<Booking> userBookings = getUserActiveBookings(userId, startTime, endTime);
        for (Booking booking : userBookings) {
            LocalDateTime bookingStart = LocalDateTime.ofInstant(booking.getFromTime(), ZoneId.systemDefault());
            LocalDateTime bookingEnd = LocalDateTime.ofInstant(booking.getToTime(), ZoneId.systemDefault());
            
            if (hasTimeOverlap(startTime, endTime, bookingStart, bookingEnd)) {
                conflicts.add(new ConflictInfo(
                    "PERSONAL_BOOKING", 
                    booking.getBookingId(), 
                    "Đặt sân cá nhân",
                    booking.getField().getLocation().getName(),
                    bookingStart,
                    bookingEnd,
                    calculateConflictSeverity(startTime, endTime, bookingStart, bookingEnd)
                ));
            }
        }
        
        return new ValidationResult(!conflicts.isEmpty(), conflicts, generateRecommendations(conflicts, startTime, endTime));
    }

    /**
     * Validates if a user can join a draft match without time conflicts
     */
    public ValidationResult validateJoinDraftMatch(Long userId, Long draftMatchId) {
        DraftMatch targetMatch = draftMatchRepository.findById(draftMatchId)
                .orElseThrow(() -> new RuntimeException("Draft match not found"));
        
        return validateTimeConflicts(userId, targetMatch.getEstimatedStartTime(), 
                targetMatch.getEstimatedEndTime(), "DRAFT_MATCH", draftMatchId);
    }

    /**
     * Validate if user can join a draft match without time conflicts
     */
    @Transactional(readOnly = true)
    public ValidationResult validateDraftMatchJoin(Long draftMatchId, Long userId) {
        DraftMatch targetMatch = draftMatchRepository.findById(draftMatchId)
                .orElseThrow(() -> new RuntimeException("Draft match not found"));
        
        return validateTimeConflicts(userId, targetMatch.getEstimatedStartTime(), 
                targetMatch.getEstimatedEndTime(), "DRAFT_MATCH", draftMatchId);
    }

    /**
     * Validate if user can join an open match without time conflicts
     */
    @Transactional(readOnly = true)
    public ValidationResult validateOpenMatchJoin(Long openMatchId, Long userId) {
        OpenMatch targetMatch = openMatchRepository.findById(openMatchId)
                .orElseThrow(() -> new RuntimeException("Open match not found"));
        
        Booking booking = targetMatch.getBooking();
        LocalDateTime startTime = LocalDateTime.ofInstant(booking.getFromTime(), ZoneId.systemDefault());
        LocalDateTime endTime = LocalDateTime.ofInstant(booking.getToTime(), ZoneId.systemDefault());
        
        return validateTimeConflicts(userId, startTime, endTime, "OPEN_MATCH", openMatchId);
    }

    /**
     * Core validation logic for time conflicts
     */
    private ValidationResult validateTimeConflicts(Long userId, LocalDateTime startTime, 
            LocalDateTime endTime, String matchType, Long excludeMatchId) {
        
        List<ConflictInfo> conflicts = new ArrayList<>();
        
        // Check draft match conflicts
        List<DraftMatch> userDraftMatches = getUserActiveDraftMatches(userId);
        for (DraftMatch match : userDraftMatches) {
            if (excludeMatchId != null && matchType.equals("DRAFT_MATCH") && match.getId().equals(excludeMatchId)) {
                continue; // Skip the target match itself
            }
            
            if (hasTimeOverlap(startTime, endTime, match.getEstimatedStartTime(), match.getEstimatedEndTime())) {
                conflicts.add(new ConflictInfo(
                    "DRAFT_MATCH", 
                    match.getId(), 
                    match.getSportType(),
                    match.getLocationDescription(),
                    match.getEstimatedStartTime(),
                    match.getEstimatedEndTime(),
                    calculateConflictSeverity(startTime, endTime, match.getEstimatedStartTime(), match.getEstimatedEndTime())
                ));
            }
        }
        
        // Check open match conflicts
        List<OpenMatch> userOpenMatches = getUserActiveOpenMatches(userId);
        for (OpenMatch match : userOpenMatches) {
            if (excludeMatchId != null && matchType.equals("OPEN_MATCH") && match.getId().equals(excludeMatchId)) {
                continue; // Skip the target match itself
            }
            
            Booking booking = match.getBooking();
            LocalDateTime matchStart = LocalDateTime.ofInstant(booking.getFromTime(), ZoneId.systemDefault());
            LocalDateTime matchEnd = LocalDateTime.ofInstant(booking.getToTime(), ZoneId.systemDefault());
            
            if (hasTimeOverlap(startTime, endTime, matchStart, matchEnd)) {
                conflicts.add(new ConflictInfo(
                    "OPEN_MATCH", 
                    match.getId(), 
                    match.getSportType(),
                    booking.getField().getLocation().getName(),
                    matchStart,
                    matchEnd,
                    calculateConflictSeverity(startTime, endTime, matchStart, matchEnd)
                ));
            }
        }
        
        // Check personal booking conflicts
        List<Booking> userBookings = getUserActiveBookings(userId, startTime, endTime);
        for (Booking booking : userBookings) {
            LocalDateTime bookingStart = LocalDateTime.ofInstant(booking.getFromTime(), ZoneId.systemDefault());
            LocalDateTime bookingEnd = LocalDateTime.ofInstant(booking.getToTime(), ZoneId.systemDefault());
            
            if (hasTimeOverlap(startTime, endTime, bookingStart, bookingEnd)) {
                conflicts.add(new ConflictInfo(
                    "PERSONAL_BOOKING", 
                    booking.getBookingId(), 
                    "Đặt sân cá nhân",
                    booking.getField().getLocation().getName(),
                    bookingStart,
                    bookingEnd,
                    calculateConflictSeverity(startTime, endTime, bookingStart, bookingEnd)
                ));
            }
        }
        
        return new ValidationResult(!conflicts.isEmpty(), conflicts, generateRecommendations(conflicts, startTime, endTime));
    }

    /**
     * Get user's active draft matches
     */
    private List<DraftMatch> getUserActiveDraftMatches(Long userId) {
        // Get draft matches where user is creator or approved participant
        List<DraftMatch> createdMatches = draftMatchRepository.findByCreatorId(userId)
                .stream()
                .filter(match -> match.getStatus() == DraftMatchStatus.RECRUITING)
                .toList();
        
        List<DraftMatch> participatingMatches = draftMatchUserStatusRepository
                .findByUserIdAndStatus(userId, "APPROVED")
                .stream()
                .map(status -> status.getDraftMatch())
                .filter(match -> match.getStatus() == DraftMatchStatus.RECRUITING || 
                               match.getStatus() == DraftMatchStatus.FULL ||
                               match.getStatus() == DraftMatchStatus.AWAITING_CONFIRMATION)
                .toList();
        
        List<DraftMatch> allMatches = new ArrayList<>(createdMatches);
        allMatches.addAll(participatingMatches);
        return allMatches;
    }

    /**
     * Get user's active open matches
     */
    private List<OpenMatch> getUserActiveOpenMatches(Long userId) {
        List<OpenMatch> createdMatches = openMatchRepository.findByCreatorUserId(userId)
                .stream()
                .filter(match -> "OPEN".equals(match.getStatus()))
                .toList();
        
        List<OpenMatch> participatingMatches = openMatchParticipantRepository
                .findByUserId(userId)
                .stream()
                .map(participant -> participant.getOpenMatch())
                .filter(match -> "OPEN".equals(match.getStatus()) || "FULL".equals(match.getStatus()))
                .toList();
        
        List<OpenMatch> allMatches = new ArrayList<>(createdMatches);
        allMatches.addAll(participatingMatches);
        return allMatches;
    }

    /**
     * Get user's active bookings in time range
     */
    private List<Booking> getUserActiveBookings(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Instant startInstant = startTime.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endTime.atZone(ZoneId.systemDefault()).toInstant();
        
        return bookingRepository.findByUserId(userId)
                .stream()
                .filter(booking -> "confirmed".equals(booking.getStatus()) &&
                        booking.getFromTime().isBefore(endInstant) &&
                        booking.getToTime().isAfter(startInstant))
                .toList();
    }

    /**
     * Check if two time ranges overlap
     */
    private boolean hasTimeOverlap(LocalDateTime start1, LocalDateTime end1, 
                                  LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    /**
     * Calculate conflict severity based on overlap duration
     */
    private ConflictSeverity calculateConflictSeverity(LocalDateTime start1, LocalDateTime end1,
                                                      LocalDateTime start2, LocalDateTime end2) {
        LocalDateTime overlapStart = start1.isAfter(start2) ? start1 : start2;
        LocalDateTime overlapEnd = end1.isBefore(end2) ? end1 : end2;
        
        long overlapMinutes = java.time.Duration.between(overlapStart, overlapEnd).toMinutes();
        long totalMinutes1 = java.time.Duration.between(start1, end1).toMinutes();
        
        double overlapPercentage = (double) overlapMinutes / totalMinutes1 * 100;
        
        if (overlapPercentage >= 80) {
            return ConflictSeverity.CRITICAL; // Almost complete overlap
        } else if (overlapPercentage >= 50) {
            return ConflictSeverity.HIGH; // Significant overlap
        } else if (overlapPercentage >= 20) {
            return ConflictSeverity.MEDIUM; // Moderate overlap
        } else {
            return ConflictSeverity.LOW; // Minor overlap
        }
    }

    /**
     * Generate recommendations based on conflicts
     */
    private List<String> generateRecommendations(List<ConflictInfo> conflicts, 
                                               LocalDateTime startTime, LocalDateTime endTime) {
        List<String> recommendations = new ArrayList<>();
        
        if (conflicts.isEmpty()) {
            return recommendations;
        }
        
        // Sort conflicts by severity
        conflicts.sort((a, b) -> b.getSeverity().ordinal() - a.getSeverity().ordinal());
        
        ConflictInfo mostSevere = conflicts.get(0);
        
        switch (mostSevere.getSeverity()) {
            case CRITICAL:
                recommendations.add("⚠️ Xung đột nghiêm trọng: Thời gian trùng lặp gần như hoàn toàn");
                recommendations.add("💡 Đề xuất: Hủy hoặc dời lịch trận đấu hiện tại trước khi tham gia");
                break;
            case HIGH:
                recommendations.add("⚠️ Xung đột cao: Thời gian trùng lặp đáng kể");
                recommendations.add("💡 Đề xuất: Kiểm tra lại lịch trình và cân nhắc điều chỉnh");
                break;
            case MEDIUM:
                recommendations.add("⚠️ Xung đột vừa phải: Có thể ảnh hưởng đến lịch trình");
                recommendations.add("💡 Đề xuất: Đảm bảo có đủ thời gian di chuyển giữa các địa điểm");
                break;
            case LOW:
                recommendations.add("ℹ️ Xung đột nhẹ: Thời gian gần nhau");
                recommendations.add("💡 Đề xuất: Lưu ý thời gian để tránh trễ hẹn");
                break;
        }
        
        // Add specific recommendations based on conflict types
        long draftMatchConflicts = conflicts.stream().filter(c -> "DRAFT_MATCH".equals(c.getType())).count();
        long openMatchConflicts = conflicts.stream().filter(c -> "OPEN_MATCH".equals(c.getType())).count();
        long bookingConflicts = conflicts.stream().filter(c -> "PERSONAL_BOOKING".equals(c.getType())).count();
        
        if (draftMatchConflicts > 0) {
            recommendations.add(String.format("📝 Bạn có %d kèo draft trùng thời gian", draftMatchConflicts));
        }
        if (openMatchConflicts > 0) {
            recommendations.add(String.format("🏟️ Bạn có %d trận đấu mở trùng thời gian", openMatchConflicts));
        }
        if (bookingConflicts > 0) {
            recommendations.add(String.format("📅 Bạn có %d lịch đặt sân cá nhân trùng thời gian", bookingConflicts));
        }
        
        return recommendations;
    }

    // Inner classes for validation results
    public static class ValidationResult {
        private boolean hasConflicts;
        private List<ConflictInfo> conflicts;
        private List<String> recommendations;
        private String message;

        public ValidationResult(boolean hasConflicts, List<ConflictInfo> conflicts, List<String> recommendations) {
            this.hasConflicts = hasConflicts;
            this.conflicts = conflicts;
            this.recommendations = recommendations;
        }

        public ValidationResult(boolean hasConflicts, String message, List<ConflictInfo> conflicts) {
            this.hasConflicts = hasConflicts;
            this.message = message;
            this.conflicts = conflicts;
            this.recommendations = new ArrayList<>();
        }

        public static ValidationResult success(String message) {
            return new ValidationResult(false, message, new ArrayList<>());
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(true, message, new ArrayList<>());
        }

        public static ValidationResult conflict(String message, List<ConflictInfo> conflicts) {
            return new ValidationResult(true, message, conflicts);
        }

        // Getters
        public boolean isHasConflicts() { return hasConflicts; }
        public List<ConflictInfo> getConflicts() { return conflicts; }
        public List<String> getRecommendations() { return recommendations; }
        public String getMessage() { return message; }
    }
    
    public static class ConflictInfo {
        private String type; // DRAFT_MATCH, OPEN_MATCH, PERSONAL_BOOKING
        private Long id;
        private String sportType;
        private String location;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private ConflictSeverity severity;
        
        public ConflictInfo(String type, Long id, String sportType, String location, 
                           LocalDateTime startTime, LocalDateTime endTime, ConflictSeverity severity) {
            this.type = type;
            this.id = id;
            this.sportType = sportType;
            this.location = location;
            this.startTime = startTime;
            this.endTime = endTime;
            this.severity = severity;
        }
        
        // Getters
        public String getType() { return type; }
        public Long getId() { return id; }
        public String getSportType() { return sportType; }
        public String getLocation() { return location; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public ConflictSeverity getSeverity() { return severity; }
    }
    
    public enum ConflictSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}