package fpt.aptech.management_field.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.aptech.management_field.services.AIRecommendationService;
import fpt.aptech.management_field.models.Booking;
import fpt.aptech.management_field.models.OpenMatch;
import fpt.aptech.management_field.models.OpenMatchParticipant;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.OpenMatchDto;
import fpt.aptech.management_field.payload.request.CreateOpenMatchRequest;
import fpt.aptech.management_field.repositories.BookingRepository;
import fpt.aptech.management_field.repositories.OpenMatchRepository;
import fpt.aptech.management_field.repositories.OpenMatchParticipantRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OpenMatchService {
    
    @Autowired
    private OpenMatchRepository openMatchRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OpenMatchParticipantRepository openMatchParticipantRepository;
    
    @Autowired
    private AIRecommendationService aiRecommendationService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public OpenMatchDto createOpenMatch(CreateOpenMatchRequest request, Long creatorUserId) {
        // Validate booking exists and belongs to user
        Optional<Booking> bookingOpt = bookingRepository.findById(request.getBookingId());
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }
        
        Booking booking = bookingOpt.get();
        if (!booking.getUser().getId().equals(creatorUserId)) {
            throw new RuntimeException("You can only create open matches for your own bookings");
        }
        
        // Check if open match already exists for this booking
        OpenMatch existingMatch = openMatchRepository.findByBookingId(request.getBookingId());
        if (existingMatch != null) {
            throw new RuntimeException("Open match already exists for this booking");
        }
        
        Optional<User> userOpt = userRepository.findById(creatorUserId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        OpenMatch openMatch = new OpenMatch();
        openMatch.setBooking(booking);
        openMatch.setCreatorUser(userOpt.get());
        openMatch.setSportType(request.getSportType());
        openMatch.setSlotsNeeded(request.getSlotsNeeded());
        
        // Convert tags list to JSON string
        try {
            String tagsJson = objectMapper.writeValueAsString(request.getRequiredTags());
            openMatch.setRequiredTags(tagsJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing required tags", e);
        }
        
        openMatch = openMatchRepository.save(openMatch);
        
        return convertToDto(openMatch);
    }
    
    public List<OpenMatchDto> getAllOpenMatches() {
        List<OpenMatch> openMatches = openMatchRepository.findAllOpenMatches();
        return openMatches.stream().map(this::convertToDto).toList();
    }
    
    public List<OpenMatchDto> getOpenMatchesBySport(String sportType) {
        List<OpenMatch> openMatches = openMatchRepository.findOpenMatchesBySportType(sportType);
        return openMatches.stream().map(this::convertToDto).toList();
    }
    
    public List<OpenMatchDto> getRankedOpenMatches(Long userId, String sportType) {
        // Get user for AI ranking
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            // If user not found, return unranked matches
            return sportType != null ? getOpenMatchesBySport(sportType) : getAllOpenMatches();
        }
        
        User user = userOpt.get();
        List<OpenMatchDto> openMatches;
        
        if (sportType != null && !sportType.isEmpty()) {
            openMatches = getOpenMatchesBySport(sportType);
        } else {
            openMatches = getAllOpenMatches();
        }
        
        // Use AI service to rank matches if available
        if (aiRecommendationService.isAIServiceAvailable() && sportType != null) {
            List<OpenMatchDto> rankedMatches = aiRecommendationService.rankOpenMatches(user, openMatches, sportType);
            
            // Debug logging to verify compatibilityScore is being set
            System.out.println("=== DEBUG: Ranked matches returned from AI service ===");
            for (OpenMatchDto match : rankedMatches) {
                System.out.println(String.format("Match ID: %d, CompatibilityScore: %s, CreatorId: %d", 
                    match.getId(), 
                    match.getCompatibilityScore() != null ? match.getCompatibilityScore().toString() : "NULL",
                    match.getCreatorUserId()));
            }
            System.out.println("=== END DEBUG ===");
            
            return rankedMatches;
        }
        
        return openMatches;
    }
    
    public List<OpenMatchDto> getUserOpenMatches(Long userId) {
        List<OpenMatch> openMatches = openMatchRepository.findByCreatorUserId(userId);
        return openMatches.stream().map(this::convertToDto).toList();
    }
    
    public void closeOpenMatch(Long matchId, Long userId) {
        Optional<OpenMatch> matchOpt = openMatchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            throw new RuntimeException("Open match not found");
        }
        
        OpenMatch match = matchOpt.get();
        if (!match.getCreatorUser().getId().equals(userId)) {
            throw new RuntimeException("You can only close your own open matches");
        }
        
        match.setStatus("CLOSED");
        openMatchRepository.save(match);
    }
    
    public void joinOpenMatch(Long matchId, Long userId) {
        Optional<OpenMatch> matchOpt = openMatchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            throw new RuntimeException("Open match not found");
        }
        
        OpenMatch match = matchOpt.get();
        if (!"OPEN".equals(match.getStatus())) {
            throw new RuntimeException("This match is no longer open for joining");
        }
        
        // Check if user is the creator (creators cannot join their own matches)
        if (match.getCreatorUser().getId().equals(userId)) {
            throw new RuntimeException("You cannot join your own match");
        }
        
        // Check if user already joined
        Optional<OpenMatchParticipant> existingParticipant = 
            openMatchParticipantRepository.findByOpenMatchIdAndUserId(matchId, userId);
        if (existingParticipant.isPresent()) {
            throw new RuntimeException("You have already joined this match");
        }
        
        // Check if match is full
        Long currentParticipants = openMatchParticipantRepository.countByOpenMatchId(matchId);
        if (currentParticipants >= match.getSlotsNeeded()) {
            throw new RuntimeException("This match is already full");
        }
        
        // Get user
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        // Create participant record
        OpenMatchParticipant participant = new OpenMatchParticipant();
        participant.setOpenMatch(match);
        participant.setUser(userOpt.get());
        openMatchParticipantRepository.save(participant);
        
        // Update match status if full
        Long newParticipantCount = openMatchParticipantRepository.countByOpenMatchId(matchId);
        if (newParticipantCount >= match.getSlotsNeeded()) {
            match.setStatus("FULL");
            openMatchRepository.save(match);
        }
    }
    
    public void leaveOpenMatch(Long matchId, Long userId) {
        Optional<OpenMatch> matchOpt = openMatchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            throw new RuntimeException("Open match not found");
        }
        
        OpenMatch match = matchOpt.get();
        
        // Find participant record
        Optional<OpenMatchParticipant> participantOpt = 
            openMatchParticipantRepository.findByOpenMatchIdAndUserId(matchId, userId);
        if (participantOpt.isEmpty()) {
            throw new RuntimeException("You are not a participant in this match");
        }
        
        // Remove participant
        openMatchParticipantRepository.delete(participantOpt.get());
        
        // Update match status if it was full
        if ("FULL".equals(match.getStatus())) {
            match.setStatus("OPEN");
            openMatchRepository.save(match);
        }
    }
    
    public OpenMatchDto getOpenMatchByBooking(Long bookingId) {
        OpenMatch openMatch = openMatchRepository.findByBookingId(bookingId);
        if (openMatch == null) {
            throw new RuntimeException("Open match not found for booking ID: " + bookingId);
        }
        return convertToDto(openMatch);
    }
    
    private OpenMatchDto convertToDto(OpenMatch openMatch) {
        OpenMatchDto dto = new OpenMatchDto();
        dto.setId(openMatch.getId());
        dto.setBookingId(openMatch.getBooking().getBookingId());
        dto.setCreatorUserId(openMatch.getCreatorUser().getId());
        dto.setCreatorUserName(openMatch.getCreatorUser().getFullName());
        
        // Set creator avatar URL if available
        if (openMatch.getCreatorUser().getImageUrl() != null) {
            dto.setCreatorAvatarUrl(openMatch.getCreatorUser().getImageUrl());
        }
        
        dto.setSportType(openMatch.getSportType());
        dto.setSlotsNeeded(openMatch.getSlotsNeeded());
        dto.setStatus(openMatch.getStatus());
        dto.setCreatedAt(openMatch.getCreatedAt());
        dto.setFieldName(openMatch.getBooking().getField().getName());
        
        // Set location information
        if (openMatch.getBooking().getField().getLocation() != null) {
            dto.setLocationName(openMatch.getBooking().getField().getLocation().getName());
            dto.setLocationAddress(openMatch.getBooking().getField().getLocation().getAddress());
        }
        
        // Set booking time information
        dto.setStartTime(openMatch.getBooking().getFromTime());
        dto.setEndTime(openMatch.getBooking().getToTime());
        
        // Extract booking date from start time
        if (openMatch.getBooking().getFromTime() != null) {
            dto.setBookingDate(openMatch.getBooking().getFromTime().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        }
        
        // Convert JSON string back to list
        try {
            List<String> tags = objectMapper.readValue(openMatch.getRequiredTags(), new TypeReference<List<String>>() {});
            dto.setRequiredTags(tags);
        } catch (JsonProcessingException e) {
            dto.setRequiredTags(new ArrayList<>());
        }
        
        // Set participant count
        Long participantCount = openMatchParticipantRepository.countByOpenMatchId(openMatch.getId());
        dto.setCurrentParticipants(participantCount.intValue());
        
        return dto;
    }
}