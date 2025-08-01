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
    
    // Open match information (if exists)
    private OpenMatchSummaryDto openMatch;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OpenMatchSummaryDto {
        private Long id;
        private String sportType;
        private Integer slotsNeeded;
        private String status;
        
        // Explicit setters for compatibility
        public void setId(Long id) {
            this.id = id;
        }
        
        public void setSportType(String sportType) {
            this.sportType = sportType;
        }
        
        public void setSlotsNeeded(Integer slotsNeeded) {
            this.slotsNeeded = slotsNeeded;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
    
    // Explicit setters for compatibility
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    
    public void setFromTime(Instant fromTime) {
        this.fromTime = fromTime;
    }
    
    public void setToTime(Instant toTime) {
        this.toTime = toTime;
    }
    
    public void setSlots(Integer slots) {
        this.slots = slots;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public void setFieldDescription(String fieldDescription) {
        this.fieldDescription = fieldDescription;
    }
    
    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    
    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }
    
    public void setFieldTypeName(String fieldTypeName) {
        this.fieldTypeName = fieldTypeName;
    }
    
    public void setFieldCategoryName(String fieldCategoryName) {
        this.fieldCategoryName = fieldCategoryName;
    }
    
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public void setDurationHours(Long durationHours) {
        this.durationHours = durationHours;
    }
    
    public void setOpenMatch(OpenMatchSummaryDto openMatch) {
        this.openMatch = openMatch;
    }
}