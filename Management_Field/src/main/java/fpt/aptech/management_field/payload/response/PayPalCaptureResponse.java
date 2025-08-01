package fpt.aptech.management_field.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayPalCaptureResponse {

    @JsonProperty("id")
    private String orderId;

    @JsonProperty("status")
    private String status;

}