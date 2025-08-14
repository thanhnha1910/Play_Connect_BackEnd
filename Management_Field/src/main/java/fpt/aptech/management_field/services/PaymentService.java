package fpt.aptech.management_field.services;

import fpt.aptech.management_field.exception.PayPalPaymentException;
import fpt.aptech.management_field.exception.ResourceNotFoundException;
import fpt.aptech.management_field.models.AdminRevenue;
import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.Payment;
import fpt.aptech.management_field.models.PaymentMethod;
import fpt.aptech.management_field.models.PaymentPayable;
import fpt.aptech.management_field.models.PaymentStatus;
import fpt.aptech.management_field.payload.response.LinkResponse;
import fpt.aptech.management_field.payload.response.PayPalCaptureResponse;
import fpt.aptech.management_field.payload.response.PayPalOrderCreationResponse;
import fpt.aptech.management_field.repositories.AdminRevenueRepository;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PayPalPaymentService payPalPaymentService;
     @Autowired
    private AdminRevenueRepository adminRevenueRepository;
    @Autowired
    private BookingRepository bookingRepository;

    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found for paymentId: " + paymentId));
    }

    public String initiatePayPal(Long payableId, PaymentPayable payableType, int amount) {
        Payment payment = createPayment(payableId, payableType, amount);
        PayPalOrderCreationResponse response = payPalPaymentService.initiatePayPalPayment(payment.getPaymentId(), amount);
        payment.setTransactionId(response.getId());
        paymentRepository.save(payment);
        return response.getLinks()
                .stream()
                .filter(link -> "payer-action".equals(link.getRel()))
                .findFirst()
                .map(LinkResponse::getHref)
                .orElseThrow(() -> new PayPalPaymentException("Approval URL not found"));
    }

    public Payment handlePayPalCallback(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found for paymentId: " + paymentId));
        PayPalCaptureResponse response = payPalPaymentService.capturePayment(payment.getTransactionId());
        if (!response.getStatus().equals("COMPLETED")) {
            throw new PayPalPaymentException("Failed to capture payment for paymentId: " + paymentId);
        }
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setUpdatedAt(LocalDateTime.now());
         payment.calculateCommission();
        Payment savedPayment = paymentRepository.save(payment);

        // Create admin revenue record
        if (payment.getPayableType() == PaymentPayable.BOOKING) {
            createAdminRevenueRecord(savedPayment);
        }
        return paymentRepository.save(payment);
    }
     private void createAdminRevenueRecord(Payment payment) {
        try {
            Booking booking = bookingRepository.findById(payment.getPayableId()).orElse(null);
            if (booking != null) {
                AdminRevenue revenue = new AdminRevenue();
                revenue.setPaymentId(payment.getPaymentId());
                revenue.setBookingId(booking.getBookingId());
                revenue.setFieldId(booking.getField().getFieldId());
                revenue.setOwnerId(booking.getField().getLocation().getOwner().getOwnerId());
                revenue.setCommissionAmount(payment.getAdminCommission());
                revenue.setBookingAmount(payment.getTotal());
                revenue.setOwnerAmount(payment.getOwnerAmount());
                revenue.setCommissionRate(payment.getCommissionRate());
                revenue.setCreatedAt(LocalDateTime.now());
                revenue.setBookingDate(LocalDateTime.ofInstant(booking.getFromTime(), java.time.ZoneId.systemDefault()));
                revenue.setFieldName(booking.getField().getName());
                revenue.setLocationName(booking.getField().getLocation().getName());

                adminRevenueRepository.save(revenue);
            }
        } catch (Exception e) {
            // Log error but don't fail the payment process
            System.err.println("Failed to create admin revenue record: " + e.getMessage());
        }
    }

    public Payment handlePayPalCancel(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found for paymentId: " + paymentId));
        payment.setStatus(PaymentStatus.CANCELED);
        payment.setUpdatedAt(LocalDateTime.now());
    payment.calculateCommission();

        return paymentRepository.save(payment);
    }

    private Payment createPayment(Long payableId, PaymentPayable payableType, int amount) {
        Payment payment = new Payment();
        payment.setTotal(amount);
        payment.setMethod(PaymentMethod.PAYPAL);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setPayableId(payableId);
        payment.setPayableType(payableType);
        // Calculate commission and owner amount immediately
        payment.calculateCommission();
        return paymentRepository.save(payment);
    }
}
