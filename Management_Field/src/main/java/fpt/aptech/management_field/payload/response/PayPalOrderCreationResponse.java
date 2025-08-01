
package fpt.aptech.management_field.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
public class PayPalOrderCreationResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("links")
    private List<LinkResponse> links;
}
