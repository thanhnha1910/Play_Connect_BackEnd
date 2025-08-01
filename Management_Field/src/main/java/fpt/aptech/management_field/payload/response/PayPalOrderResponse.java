package fpt.aptech.management_field.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class PayPalOrderResponse {
    private String status;
    private PayPalPayerResponse payer;
}