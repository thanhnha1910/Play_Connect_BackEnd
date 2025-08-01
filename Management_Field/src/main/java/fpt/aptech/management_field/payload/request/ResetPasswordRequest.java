package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResetPasswordRequest {
    @NotBlank
    private String token;
    
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
    
    @NotBlank
    @Size(min = 6, max = 40)
    private String confirmPassword;
    
    // Getters
    public String getToken() { return token; }
    public String getPassword() { return password; }
    public String getConfirmPassword() { return confirmPassword; }
    
    // Setters
    public void setToken(String token) { this.token = token; }
    public void setPassword(String password) { this.password = password; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}