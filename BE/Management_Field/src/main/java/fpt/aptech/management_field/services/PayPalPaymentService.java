package fpt.aptech.management_field.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PayPalPaymentService {

    @Value("${paypal.mode}")
    private String mode;

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.url}")
    private String paypalUrl;

    private final RestTemplate restTemplate;

    public PayPalPaymentService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public String initiatePayPalPayment(Long bookingId, float amount) {
        String accessToken = getAccessToken();
        String orderId = "BOOKING_" + bookingId;
        String description = "Thanh toán đặt sân Booking ID: " + bookingId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("intent", "CAPTURE");
        requestBody.put("purchase_units", Collections.singletonList(Map.of(
                "amount", Map.of(
                        "currency_code", "USD",
                        "value", String.format("%.2f", amount)
                ),
                "description", description,
                "reference_id", orderId
        )));
        requestBody.put("payment_source", Map.of("paypal", new HashMap<>()));
        requestBody.put("application_context", Map.of(
                "return_url", "http://localhost:3000/booking/success?bookingId=" + bookingId,
                "cancel_url", "http://localhost:3000/booking/cancel?bookingId=" + bookingId
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String url = paypalUrl + "/v2/checkout/orders";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> body = response.getBody();
            System.out.println("PayPal Response: " + body);
            return ((List<Map<String, Object>>) body.get("links"))
                    .stream()
                    .filter(link -> "payer-action".equals(link.get("rel")))
                    .findFirst()
                    .map(link -> (String) link.get("href"))
                    .orElseThrow(() -> new RuntimeException("Approval URL not found"));
        } else {
            System.err.println("PayPal Error: " + response.getStatusCode() + " - " + response.getBody());
            throw new RuntimeException("PayPal payment initiation failed: " + response.getBody());
        }
    }

    public void capturePayment(Long bookingId, String token, String payerId) {
        String accessToken = getAccessToken();
        
        // Use the token as orderId since PayPal returns the actual order ID in the token parameter
        String orderId = token;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>("", headers);
        String url = paypalUrl + "/v2/checkout/orders/" + orderId + "/capture";
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("PayPal Capture Success: " + response.getBody());
            } else {
                System.err.println("PayPal Capture Error: " + response.getStatusCode() + " - " + response.getBody());
                throw new RuntimeException("PayPal payment capture failed: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("PayPal Capture Exception: " + e.getMessage());
            throw new RuntimeException("PayPal payment capture failed: " + e.getMessage());
        }
    }

    public boolean verifyPaymentWithPayPal(String orderId, String payerId) {
        try {
            String accessToken = getAccessToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            HttpEntity<String> entity = new HttpEntity<>("", headers);
            String url = paypalUrl + "/v2/checkout/orders/" + orderId;
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> orderDetails = response.getBody();
                String status = (String) orderDetails.get("status");
                
                // Verify that the order is approved and ready for capture
                boolean isApproved = "APPROVED".equals(status);
                
                // Additional verification: check if payer information matches
                Map<String, Object> payer = (Map<String, Object>) orderDetails.get("payer");
                boolean payerMatches = payer != null && payerId.equals(payer.get("payer_id"));
                
                System.out.println("PayPal Order Verification - Status: " + status + ", PayerID Match: " + payerMatches);
                
                return isApproved && payerMatches;
            } else {
                System.err.println("PayPal Verification Error: " + response.getStatusCode() + " - " + response.getBody());
                return false;
            }
        } catch (Exception e) {
            System.err.println("PayPal Verification Exception: " + e.getMessage());
            return false;
        }
    }

    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        String tokenUrl = paypalUrl + "/v1/oauth2/token";
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return (String) response.getBody().get("access_token");
        } else {
            System.err.println("Token Error: " + response.getStatusCode() + " - " + response.getBody());
            throw new RuntimeException("Failed to get PayPal access token: " + response.getBody());
        }
    }
}