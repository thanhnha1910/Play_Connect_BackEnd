package fpt.aptech.management_field.services;

import fpt.aptech.management_field.payload.response.GeocodeResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class GeocodingService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    // Using OpenStreetMap Nominatim (free alternative to Google Geocoding)
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    
    public GeocodeResponse geocodeAddress(String address) {
        try {
            String url = NOMINATIM_URL + "?q=" + address + "&format=json&limit=1";
            
            // This is a simplified implementation
            // In production, you should use proper JSON parsing and error handling
            GeocodeResponse response = new GeocodeResponse();
            response.setError("Geocoding service not fully implemented. Please use Google Geocoding API or similar service.");
            
            return response;
        } catch (Exception e) {
            GeocodeResponse errorResponse = new GeocodeResponse();
            errorResponse.setError("Failed to geocode address: " + e.getMessage());
            return errorResponse;
        }
    }
}