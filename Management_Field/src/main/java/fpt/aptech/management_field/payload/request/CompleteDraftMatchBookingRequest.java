package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class CompleteDraftMatchBookingRequest {
    
    @NotNull(message = "Booking ID is required")
    private Long bookingId;
    
    @NotBlank(message = "Payment confirmation is required")
    private String paymentConfirmation;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;
    
    @NotBlank(message = "Payment status is required")
    private String paymentStatus;
    
    private String transactionId;
    
    private String notes;
    
    // Constructors
    public CompleteDraftMatchBookingRequest() {}
    
    public CompleteDraftMatchBookingRequest(Long bookingId, String paymentConfirmation, 
                                          BigDecimal totalAmount, String paymentStatus, 
                                          String transactionId, String notes) {
        this.bookingId = bookingId;
        this.paymentConfirmation = paymentConfirmation;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.transactionId = transactionId;
        this.notes = notes;
    }
    
    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    
    public String getPaymentConfirmation() {
        return paymentConfirmation;
    }
    
    public void setPaymentConfirmation(String paymentConfirmation) {
        this.paymentConfirmation = paymentConfirmation;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}