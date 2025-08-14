package fpt.aptech.management_field.payload.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class BatchBookingRequest {
    @NotEmpty(message = "Booking requests cannot be empty")
    @Valid
    private List<BookingRequest> bookingRequests;

    public BatchBookingRequest() {}

    public BatchBookingRequest(List<BookingRequest> bookingRequests) {
        this.bookingRequests = bookingRequests;
    }

    public List<BookingRequest> getBookingRequests() {
        return bookingRequests;
    }

    public void setBookingRequests(List<BookingRequest> bookingRequests) {
        this.bookingRequests = bookingRequests;
    }
}