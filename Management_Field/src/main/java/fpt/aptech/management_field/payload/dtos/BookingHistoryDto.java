package fpt.aptech.management_field.payload.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingHistoryDto {
    private Long bookingId;
    private String fieldName;
    private String fieldAddress;
    private String coverImageUrl;
    private Instant startTime;
    private Instant endTime;
    private Double totalPrice;
    private String status;
    
    // Explicit setters for compatibility
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    
    public void setFieldAddress(String fieldAddress) {
        this.fieldAddress = fieldAddress;
    }
    
    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }
    
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }
    
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
    
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}