package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.payload.dtos.ChatbotRequestDTO;
import fpt.aptech.management_field.payload.dtos.ChatbotResponseDTO;
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
    @Operation(summary = "Send message to chatbot v2.0", description = "Send a user message to the AI chatbot and get intelligent response with context support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed message"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ChatbotResponseDTO> sendMessage(
            @Parameter(description = "Request body containing user message and optional context", required = true)
            @RequestBody ChatbotRequestDTO request) {
        
        try {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                ChatbotResponseDTO errorResponse = new ChatbotResponseDTO();
                errorResponse.setText("Message cannot be empty");
                errorResponse.setSessionId(request.getSessionId());
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            ChatbotResponseDTO response = chatbotService.processMessage(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ChatbotResponseDTO errorResponse = new ChatbotResponseDTO();
            errorResponse.setText("Unable to process your request at the moment");
            errorResponse.setSessionId(request.getSessionId());
            return ResponseEntity.internalServerError().body(errorResponse);
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