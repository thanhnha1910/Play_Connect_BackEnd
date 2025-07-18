package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.Sport;
import fpt.aptech.management_field.repositories.SportRepository;
import fpt.aptech.management_field.repositories.TagRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/sports")
@Tag(name = "Sports", description = "Public APIs for sports data - API công khai cho dữ liệu môn thể thao")
public class PublicSportsController {

    @Autowired
    private SportRepository sportRepository;
    
    @Autowired
    private TagRepository tagRepository;

    @GetMapping
    @Operation(summary = "Get all active sports", description = "Retrieve all active sports for public use")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active sports list")
    })
    public ResponseEntity<List<Sport>> getAllSports() {
        List<Sport> activeSports = sportRepository.findByIsActiveTrue();
        return ResponseEntity.ok(activeSports);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active sports", description = "Retrieve all active sports for public use")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active sports list")
    })
    public ResponseEntity<List<Sport>> getActiveSports() {
        List<Sport> activeSports = sportRepository.findByIsActiveTrue();
        return ResponseEntity.ok(activeSports);
    }

    @GetMapping("/search")
    @Operation(summary = "Search sports by name", description = "Search for sports by name (case-insensitive)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved matching sports")
    })
    public ResponseEntity<List<Sport>> searchSportsByName(
            @Parameter(description = "Search query", required = true)
            @RequestParam String name) {
        List<Sport> sports = sportRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name);
        return ResponseEntity.ok(sports);
    }

    @GetMapping("/code/{sportCode}")
    @Operation(summary = "Get sport by code", description = "Retrieve a specific sport by its sport code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved sport"),
        @ApiResponse(responseCode = "404", description = "Sport not found")
    })
    public ResponseEntity<Sport> getSportByCode(
            @Parameter(description = "Sport code", required = true)
            @PathVariable String sportCode) {
        Optional<Sport> sport = sportRepository.findBySportCodeAndIsActiveTrue(sportCode);
        return sport.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sport by ID", description = "Retrieve a specific sport by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved sport"),
        @ApiResponse(responseCode = "404", description = "Sport not found")
    })
    public ResponseEntity<Sport> getSportById(
            @Parameter(description = "Sport ID", required = true)
            @PathVariable Long id) {
        Optional<Sport> sport = sportRepository.findById(id);
        return sport.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/tags")
    @Operation(summary = "Get all active tags", description = "Retrieve all active tags for public use")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active tags list")
    })
    public ResponseEntity<List<fpt.aptech.management_field.models.Tag>> getActiveTags() {
        List<fpt.aptech.management_field.models.Tag> activeTags = tagRepository.findAllActiveOrderByName();
        return ResponseEntity.ok(activeTags);
    }
    
    @GetMapping("/tags/{sportId}")
    @Operation(summary = "Get tags by sport ID", description = "Retrieve all active tags for a specific sport")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tags for sport")
    })
    public ResponseEntity<List<fpt.aptech.management_field.models.Tag>> getTagsBySport(
            @Parameter(description = "Sport ID", required = true)
            @PathVariable Long sportId) {
        List<fpt.aptech.management_field.models.Tag> tags = tagRepository.findActiveBySportIdOrderByName(sportId);
        return ResponseEntity.ok(tags);
    }
    
    @GetMapping("/tags/code/{sportCode}")
    @Operation(summary = "Get tags by sport code", description = "Retrieve all active tags for a specific sport by sport code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tags for sport"),
        @ApiResponse(responseCode = "404", description = "Sport not found")
    })
    public ResponseEntity<List<fpt.aptech.management_field.models.Tag>> getTagsBySportCode(
            @Parameter(description = "Sport code", required = true)
            @PathVariable String sportCode) {
        Optional<Sport> sport = sportRepository.findBySportCodeAndIsActiveTrue(sportCode);
        if (sport.isPresent()) {
            List<fpt.aptech.management_field.models.Tag> tags = tagRepository.findActiveBySportIdOrderByName(sport.get().getId());
            return ResponseEntity.ok(tags);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}