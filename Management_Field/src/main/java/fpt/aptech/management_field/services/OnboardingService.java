package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.Sport;
import fpt.aptech.management_field.models.Tag;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.SportProfileDto;
import fpt.aptech.management_field.payload.request.OnboardingRequest;
import fpt.aptech.management_field.repositories.SportRepository;
import fpt.aptech.management_field.repositories.TagRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OnboardingService {
    
    @Autowired
    private SportRepository sportRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public void processOnboarding(User user, OnboardingRequest request) throws Exception {
        System.out.println("DEBUG: OnboardingService.processOnboarding called");
        System.out.println("DEBUG: User: " + user.getEmail());
        System.out.println("DEBUG: Request sport profiles count: " + (request.getSportProfiles() != null ? request.getSportProfiles().size() : 0));
        System.out.println("DEBUG: Request isDiscoverable: " + request.getIsDiscoverable());
        
        if (request.getSportProfiles() == null || request.getSportProfiles().isEmpty()) {
            throw new Exception("No sport profiles provided");
        }
        
        Map<String, SportProfileDto> sportProfiles = new HashMap<>();
        
        for (OnboardingRequest.SportSelectionDto sportSelection : request.getSportProfiles()) {
            System.out.println("DEBUG: Processing sport: " + sportSelection.getSportName());
            System.out.println("DEBUG: Sport skill: " + sportSelection.getSkill());
            System.out.println("DEBUG: Sport tags count: " + (sportSelection.getTags() != null ? sportSelection.getTags().size() : 0));
            
            // Handle sport creation/retrieval
            Sport sport = getOrCreateSport(sportSelection);
            System.out.println("DEBUG: Sport processed: " + sport.getName() + " (" + sport.getSportCode() + ")");
            
            // Handle tags creation/retrieval
            List<String> tagNames = new ArrayList<>();
            for (OnboardingRequest.TagSelectionDto tagSelection : sportSelection.getTags()) {
                System.out.println("DEBUG: Processing tag: " + tagSelection.getTagName());
                Tag tag = getOrCreateTag(tagSelection, sport);
                tagNames.add(tag.getName());
                System.out.println("DEBUG: Tag processed: " + tag.getName());
            }
            
            // Create SportProfileDto
            SportProfileDto sportProfile = new SportProfileDto(
                sport.getSportCode(),
                sportSelection.getSkill(),
                tagNames
            );
            
            sportProfiles.put(sport.getSportCode(), sportProfile);
            System.out.println("DEBUG: SportProfile created for: " + sport.getSportCode());
        }
        
        // Convert to JSON and save
        String sportProfilesJson = objectMapper.writeValueAsString(sportProfiles);
        System.out.println("DEBUG: SportProfiles JSON: " + sportProfilesJson);
        user.setSportProfiles(sportProfilesJson);
        
        // Set discoverable flag
        if (request.getIsDiscoverable() != null) {
            user.setIsDiscoverable(request.getIsDiscoverable());
            System.out.println("DEBUG: Set isDiscoverable to: " + request.getIsDiscoverable());
        }
        
        // Mark onboarding as completed
        user.setHasCompletedProfile(true);
        System.out.println("DEBUG: Set hasCompletedProfile to true");
        
        userRepository.save(user);
        System.out.println("DEBUG: User saved successfully");
    }
    
    private Sport getOrCreateSport(OnboardingRequest.SportSelectionDto sportSelection) {
        if (sportSelection.getSportId() != null) {
            // Use existing sport
            return sportRepository.findById(sportSelection.getSportId())
                .orElseThrow(() -> new RuntimeException("Sport not found with ID: " + sportSelection.getSportId()));
        } else {
            // Create new sport
            String sportName = sportSelection.getSportName();
            String sportCode = generateSportCode(sportName);
            
            // Check if sport with this code already exists
            Optional<Sport> existingSport = sportRepository.findBySportCode(sportCode);
            if (existingSport.isPresent()) {
                return existingSport.get();
            }
            
            Sport newSport = new Sport();
            newSport.setName(sportName);
            newSport.setSportCode(sportCode);
            newSport.setIcon(sportSelection.getSportIcon() != null ? sportSelection.getSportIcon() : "🏃");
            newSport.setIsActive(true);
            
            return sportRepository.save(newSport);
        }
    }
    
    private Tag getOrCreateTag(OnboardingRequest.TagSelectionDto tagSelection, Sport sport) {
        if (tagSelection.getTagId() != null) {
            // Use existing tag
            return tagRepository.findById(tagSelection.getTagId())
                .orElseThrow(() -> new RuntimeException("Tag not found with ID: " + tagSelection.getTagId()));
        } else {
            // Create new tag
            String tagName = tagSelection.getTagName();
            
            // Check if tag with this name already exists for this sport
            Optional<Tag> existingTag = tagRepository.findByNameAndSport(tagName, sport);
            if (existingTag.isPresent()) {
                return existingTag.get();
            }
            
            Tag newTag = new Tag();
            newTag.setName(tagName);
            newTag.setSport(sport);
            newTag.setIsActive(true);
            
            return tagRepository.save(newTag);
        }
    }
    
    private String generateSportCode(String sportName) {
        // Convert to uppercase and replace spaces/special chars with underscores
        return sportName.toUpperCase()
            .replaceAll("[^A-Z0-9]", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", "");
    }
}