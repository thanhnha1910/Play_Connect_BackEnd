package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Future;
import java.time.LocalDateTime;

public class InitiateDraftMatchBookingRequest {
    
    @NotNull(message = "Field ID is required")
    private Long fieldId;
    
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    private String notes;
    
    // Constructors
    public InitiateDraftMatchBookingRequest() {}
    
    public InitiateDraftMatchBookingRequest(Long fieldId, LocalDateTime startTime, 
                                          LocalDateTime endTime, String paymentMethod, String notes) {
        this.fieldId = fieldId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }
    
    // Getters and Setters
    public Long getFieldId() {
        return fieldId;
    }
    
    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}