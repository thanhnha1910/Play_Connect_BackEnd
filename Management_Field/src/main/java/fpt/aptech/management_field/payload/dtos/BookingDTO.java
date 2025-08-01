package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {
    private Instant fromTime;
    private Instant toTime;
    private Integer slots;
    private String status;
    private String customerName;
    private String customerPhone;
    private boolean isBooked;
    
     private int basePrice;
    private int discountPercent;
    private int discountAmount;
    private int totalPrice;

    // Explicit setters for compatibility
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
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public void setBooked(boolean booked) {
        this.isBooked = booked;
    }
}
