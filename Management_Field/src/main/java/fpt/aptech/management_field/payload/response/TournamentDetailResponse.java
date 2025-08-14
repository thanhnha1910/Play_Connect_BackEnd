package fpt.aptech.management_field.payload.response;

import fpt.aptech.management_field.payload.dtos.LocationDto;
import fpt.aptech.management_field.payload.dtos.TeamDto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for tournament details including participating teams and financial info
 */
@Data
public class TournamentDetailResponse {
    
    private Long tournamentId;
    private String name;
    private String slug;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer prize;
    private Integer entryFee;
    private Integer slots;
    private String status;
    private String coverImage;
    private LocationDto location;
    private List<ParticipatingTeamInfo> participatingTeams;
    private FinancialSummary financialSummary;
    
    @Data
    public static class ParticipatingTeamInfo {
        private Long participatingTeamId;
        private TeamDto team;
        private String status; // registered, paid, cancelled
        private LocalDateTime registrationDate;
        private LocalDateTime paymentDate;
        private String paymentStatus; // pending, completed, failed
    }
    
    @Data
    public static class FinancialSummary {
        private Integer totalRevenue; // entryFee * paid teams
        private Integer totalExpenses; // prize amount
        private Integer netProfit; // totalRevenue - totalExpenses
        private Integer registeredTeams;
        private Integer paidTeams;
        private Integer pendingPayments;
    }
}