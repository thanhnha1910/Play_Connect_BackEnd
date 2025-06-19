package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuth2Request {
    @NotBlank
    private String code;
}