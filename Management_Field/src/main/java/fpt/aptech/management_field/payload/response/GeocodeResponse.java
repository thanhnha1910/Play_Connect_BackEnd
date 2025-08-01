package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeocodeResponse {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String formattedAddress;
    private String error;
    
    // Explicit setter for compatibility
    public void setError(String error) {
        this.error = error;
    }
}