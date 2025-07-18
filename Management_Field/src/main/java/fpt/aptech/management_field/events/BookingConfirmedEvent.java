package fpt.aptech.management_field.events;

import fpt.aptech.management_field.models.Booking;
import org.springframework.context.ApplicationEvent;

public class BookingConfirmedEvent extends ApplicationEvent {
    private final Booking booking;
    
    public BookingConfirmedEvent(Object source, Booking booking) {
        super(source);
        this.booking = booking;
    }
    
    public Booking getBooking() {
        return booking;
    }
}