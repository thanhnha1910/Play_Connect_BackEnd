package fpt.aptech.management_field.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.aptech.management_field.events.BookingConfirmedEvent;
import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.SportProfileDto;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ImplicitDataCollectionService {

    @Autowired
    private UserRepository userRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @EventListener
    @Transactional
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        Booking booking = event.getBooking();
        User user = booking.getUser();
        Field field = booking.getField();
        
        // Generate implicit tags based on booking behavior
        List<String> implicitTags = generateImplicitTags(booking, field);
        
        if (!implicitTags.isEmpty()) {
            updateUserSportProfilesWithImplicitTags(user, implicitTags);
        }
    }
    
    private List<String> generateImplicitTags(Booking booking, Field field) {
        List<String> tags = new ArrayList<>();
        
        // Add location-based tags
        if (field.getLocation() != null && field.getLocation().getName() != null) {
            String locationName = field.getLocation().getName().toLowerCase();
            
            // Extract district/area information
            if (locationName.contains("quận") || locationName.contains("district")) {
                tags.add(extractDistrict(locationName));
            }
            
            // Add general location preference
            tags.add("khu vực " + field.getLocation().getName());
        }
        
        // Add time-based preferences
        LocalTime bookingTime = booking.getFromTime().atZone(ZoneId.systemDefault()).toLocalTime();
        if (bookingTime.isBefore(LocalTime.of(12, 0))) {
            tags.add("chơi sáng");
        } else if (bookingTime.isBefore(LocalTime.of(18, 0))) {
            tags.add("chơi chiều");
        } else {
            tags.add("chơi tối");
        }
        
        // Add field type preferences
        if (field.getName() != null) {
            String fieldName = field.getName().toLowerCase();
            if (fieldName.contains("5") || fieldName.contains("năm")) {
                tags.add("sân 5");
            } else if (fieldName.contains("7") || fieldName.contains("bảy")) {
                tags.add("sân 7");
            } else if (fieldName.contains("11") || fieldName.contains("mười một")) {
                tags.add("sân 11");
            }
        }
        
        // Add frequency-based tags (if user books frequently)
        tags.add("thường xuyên");
        
        return tags;
    }
    
    private String extractDistrict(String locationName) {
        // Simple extraction logic for Vietnamese districts
        if (locationName.contains("quận 1") || locationName.contains("district 1")) {
            return "quận 1";
        } else if (locationName.contains("quận 2") || locationName.contains("district 2")) {
            return "quận 2";
        } else if (locationName.contains("quận 3") || locationName.contains("district 3")) {
            return "quận 3";
        } else if (locationName.contains("quận 7") || locationName.contains("district 7")) {
            return "quận 7";
        } else if (locationName.contains("quận 10") || locationName.contains("district 10")) {
            return "quận 10";
        }
        // Add more districts as needed
        return "khu vực khác";
    }
    
    private void updateUserSportProfilesWithImplicitTags(User user, List<String> implicitTags) {
        try {
            Map<String, SportProfileDto> sportProfiles = new HashMap<>();
            
            // Parse existing sport profiles
            if (user.getSportProfiles() != null && !user.getSportProfiles().isEmpty()) {
                sportProfiles = objectMapper.readValue(
                    user.getSportProfiles(), 
                    new TypeReference<Map<String, SportProfileDto>>() {}
                );
            }
            
            // Default sport type (assuming football/soccer is most common)
            String defaultSport = "BONG_DA";
            
            // Get or create sport profile for the default sport
            SportProfileDto profile = sportProfiles.getOrDefault(defaultSport, new SportProfileDto());
            if (profile.getTags() == null) {
                profile.setTags(new ArrayList<>());
            }
            
            // Add implicit tags (avoid duplicates)
            for (String tag : implicitTags) {
                if (!profile.getTags().contains(tag)) {
                    profile.getTags().add(tag);
                }
            }
            
            // Set default skill level if not set
            if (profile.getSkill() == null) {
                profile.setSkill(3); // Default to intermediate level
            }
            
            sportProfiles.put(defaultSport, profile);
            
            // Save updated sport profiles
            String updatedSportProfilesJson = objectMapper.writeValueAsString(sportProfiles);
            user.setSportProfiles(updatedSportProfilesJson);
            
            userRepository.save(user);
            
        } catch (JsonProcessingException e) {
            // Log error but don't fail the booking process
            System.err.println("Error updating sport profiles with implicit tags: " + e.getMessage());
        }
    }
}