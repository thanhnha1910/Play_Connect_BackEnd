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
    
    @Transactional
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
            
            // Handle tags creation/retrieval with "find or create" logic
            Set<Tag> managedTags = new HashSet<>();
            List<String> tagNames = new ArrayList<>();
            
            if (sportSelection.getTags() != null) {
                for (OnboardingRequest.TagSelectionDto tagSelection : sportSelection.getTags()) {
                    System.out.println("DEBUG: Processing tag: " + tagSelection.getTagName());
                    
                    if (tagSelection.getTagId() != null) {
                        // Use existing tag by ID
                        Tag existingTag = tagRepository.findById(tagSelection.getTagId())
                            .orElseThrow(() -> new RuntimeException("Tag not found with ID: " + tagSelection.getTagId()));
                        managedTags.add(existingTag);
                        tagNames.add(existingTag.getName());
                        System.out.println("DEBUG: Using existing tag by ID: " + existingTag.getName());
                    } else {
                        // Implement "find or create" logic for new tags
                        String tagName = tagSelection.getTagName();
                        String sportCode = sport.getSportCode();
                        
                        // 1. Check if the tag already exists in the database
                        Optional<Tag> existingTag = tagRepository.findByNameAndSport(tagName, sport);
                        
                        if (existingTag.isPresent()) {
                            // 2a. If it exists, use the managed entity from the database
                            managedTags.add(existingTag.get());
                            tagNames.add(existingTag.get().getName());
                            System.out.println("DEBUG: Found existing tag: " + existingTag.get().getName());
                        } else {
                            // 2b. If it does NOT exist, create a new Tag entity with improved race condition handling
                            try {
                                Tag newTag = new Tag();
                                newTag.setName(tagName);
                                newTag.setSport(sport);
                                newTag.setIsActive(true);
                                
                                // Save the new tag to the DB to get an ID before associating it
                                Tag savedTag = tagRepository.save(newTag);
                                managedTags.add(savedTag);
                                tagNames.add(savedTag.getName());
                                System.out.println("DEBUG: Created new tag: " + savedTag.getName() + " with ID: " + savedTag.getId());
                            } catch (Exception e) {
                                // Handle duplicate key constraint violation or other database errors
                                System.out.println("DEBUG: Tag creation failed, attempting to fetch existing tag: " + e.getMessage());
                                
                                // Try to find the tag that might have been created by another thread
                                Optional<Tag> raceConditionTag = tagRepository.findByNameAndSport(tagName, sport);
                                if (raceConditionTag.isPresent()) {
                                    managedTags.add(raceConditionTag.get());
                                    tagNames.add(raceConditionTag.get().getName());
                                    System.out.println("DEBUG: Found tag created by another thread: " + raceConditionTag.get().getName());
                                } else {
                                    // If still not found, provide more detailed error information
                                    throw new RuntimeException("Failed to create or find tag: " + tagName + " for sport: " + sport.getName() + ". Original error: " + e.getMessage(), e);
                                }
                            }
                        }
                    }
                }
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
    

    
    private String generateSportCode(String sportName) {
        // Convert to uppercase and replace spaces/special chars with underscores
        return sportName.toUpperCase()
            .replaceAll("[^A-Z0-9]", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", "");
    }
}