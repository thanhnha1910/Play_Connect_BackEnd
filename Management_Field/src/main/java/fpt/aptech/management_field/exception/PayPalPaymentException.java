package fpt.aptech.management_field.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class PayPalPaymentException extends ApiBaseException {
    public PayPalPaymentException(String message) {
        super(message);
    }
}
