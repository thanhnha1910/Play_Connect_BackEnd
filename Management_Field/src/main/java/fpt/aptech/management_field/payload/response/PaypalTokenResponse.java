package fpt.aptech.management_field.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaypalTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
}
