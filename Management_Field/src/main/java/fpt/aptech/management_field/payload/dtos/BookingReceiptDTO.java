package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingReceiptDTO {
    private Long bookingId;
    private Instant fromTime;
    private Instant toTime;
    private Integer slots;
    private String status;
    private String paymentToken;
    
    // User information
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    // Field information
    private Long fieldId;
    private String fieldName;
    private String fieldDescription;
    private Double hourlyRate;
    
    // Location information
    private String locationName;
    private String locationAddress;
    
    // Field type and category
    private String fieldTypeName;
    private String fieldCategoryName;
    
    // Calculated values
    private Double totalPrice;
    private Long durationHours;
}