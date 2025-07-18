package fpt.aptech.management_field.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;
    private String status;
    private boolean hasCompletedProfile;

    public JwtResponse(String accessToken, String refreshToken, Long id, String username, String email, String fullName, List<String> roles, String status, boolean hasCompletedProfile) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
        this.status = status;
        this.hasCompletedProfile = hasCompletedProfile;
    }
}