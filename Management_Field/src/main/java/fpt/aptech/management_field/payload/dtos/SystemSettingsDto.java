package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsDto {
    private Boolean maintenanceMode;
    private String defaultCurrency;
    private Integer maxBookingHours;
    private String siteName;
    private String supportEmail;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Double platformCommission;
    private Integer maxCancellationHours;
    private Boolean autoApproveBookings;
}