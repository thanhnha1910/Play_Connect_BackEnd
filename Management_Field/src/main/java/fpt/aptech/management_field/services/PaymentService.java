package fpt.aptech.management_field.services;

import fpt.aptech.management_field.exception.PayPalPaymentException;
import fpt.aptech.management_field.exception.ResourceNotFoundException;
import fpt.aptech.management_field.models.Payment;
import fpt.aptech.management_field.models.PaymentMethod;
import fpt.aptech.management_field.models.PaymentPayable;
import fpt.aptech.management_field.models.PaymentStatus;
import fpt.aptech.management_field.payload.response.LinkResponse;
import fpt.aptech.management_field.payload.response.PayPalCaptureResponse;
import fpt.aptech.management_field.payload.response.PayPalOrderCreationResponse;
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
        return paymentRepository.save(payment);
    }

    public Payment handlePayPalCancel(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment not found for paymentId: " + paymentId));
        payment.setStatus(PaymentStatus.CANCELED);
        payment.setUpdatedAt(LocalDateTime.now());
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
        return paymentRepository.save(payment);
    }
}
