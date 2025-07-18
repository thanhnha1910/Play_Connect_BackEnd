package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2Response {
    private String message;
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private Boolean hasCompletedProfile;
    
    // User data fields
    private Long id;
    private String email;
    private String fullName;
    private String username;
    private String imageUrl;
    private Set<String> roles;
    private Boolean emailVerified;
    private Boolean active;
    
    public OAuth2Response(String message, String token) {
        this.message = message;
        this.token = token;
    }
}