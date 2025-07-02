package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2Response {
    private String message;
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    
    public OAuth2Response(String message, String token) {
        this.message = message;
        this.token = token;
    }
}