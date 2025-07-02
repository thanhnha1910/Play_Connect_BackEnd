package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerAnalyticsDto {
    private long totalOwners;
    private long activeOwners;
    private long pendingApprovalCount;
    private long suspendedCount;
    private List<RecentActivityDto> recentActivities;
    
    // Constructor without recent activities for basic analytics
    public OwnerAnalyticsDto(long totalOwners, long activeOwners, long pendingApprovalCount, long suspendedCount) {
        this.totalOwners = totalOwners;
        this.activeOwners = activeOwners;
        this.pendingApprovalCount = pendingApprovalCount;
        this.suspendedCount = suspendedCount;
    }
}