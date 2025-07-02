package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.services.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "Chatbot", description = "AI Chatbot APIs for customer support")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/query")
    @Operation(summary = "Send message to chatbot", description = "Send a user message to the AI chatbot and get intelligent response")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed message"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, String>> sendMessage(
            @Parameter(description = "Request body containing user message", required = true)
            @RequestBody Map<String, String> request) {
        
        try {
            String message = request.get("message");
            
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message cannot be empty"));
            }
            
            String response = chatbotService.getResponse(message.trim());
            
            return ResponseEntity.ok(Map.of(
                "response", response,
                "status", "success"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Internal server error",
                    "message", "Unable to process your request at the moment"
                ));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Check chatbot health", description = "Check if the chatbot service is running properly")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chatbot service is healthy")
    })
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "message", "Chatbot service is running",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}