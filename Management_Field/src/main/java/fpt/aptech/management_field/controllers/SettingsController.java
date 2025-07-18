package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.payload.dtos.SystemSettingsDto;
import fpt.aptech.management_field.payload.response.MessageResponse;
import fpt.aptech.management_field.services.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @GetMapping("/settings")
    public ResponseEntity<?> getSystemSettings() {
        try {
            SystemSettingsDto settings = settingsService.getSystemSettings();
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSystemSettings(@RequestBody SystemSettingsDto settingsDto) {
        try {
            SystemSettingsDto updatedSettings = settingsService.updateSystemSettings(settingsDto);
            return ResponseEntity.ok(updatedSettings);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}