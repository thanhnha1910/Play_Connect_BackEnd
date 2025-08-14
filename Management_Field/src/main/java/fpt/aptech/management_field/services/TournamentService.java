package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.ParticipatingTeam;

import fpt.aptech.management_field.models.PaymentPayable;
import fpt.aptech.management_field.models.Team;
import fpt.aptech.management_field.models.TeamRoster;
import fpt.aptech.management_field.models.Tournament;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.dtos.LocationDto;
import fpt.aptech.management_field.payload.dtos.TournamentDto;
import fpt.aptech.management_field.payload.request.TournamentRegistrationRequest;
import fpt.aptech.management_field.repositories.LocationRepository;
import fpt.aptech.management_field.repositories.ParticipatingTeamRepository;
import fpt.aptech.management_field.repositories.TeamRepository;
import fpt.aptech.management_field.repositories.TournamentRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import fpt.aptech.management_field.services.PayPalPaymentService;
import org.springframework.transaction.annotation.Transactional;
import fpt.aptech.management_field.mappers.TeamMapper;
import fpt.aptech.management_field.mappers.TournamentMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TournamentService {
    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ParticipatingTeamRepository participatingTeamRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private TournamentMapper tournamentMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TournamentDto> getAllTournaments() {
        return tournamentRepository.findAll().stream()
                .map(tournamentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> registerTeamForTournament(TournamentRegistrationRequest request) {
        Tournament tournament = tournamentRepository.findById(request.getTournamentId())
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (participatingTeamRepository.findByTeamIdAndTournamentId(team.getTeamId(), tournament.getTournamentId()).isPresent()) {
            throw new RuntimeException("Team already registered for this tournament.");
        }

        ParticipatingTeam participatingTeam = new ParticipatingTeam();
        participatingTeam.setTeam(team);
        participatingTeam.setTournament(tournament);
        participatingTeam.setStatus("PENDING");
        participatingTeamRepository.save(participatingTeam);

        double entryFee = tournament.getEntryFee();
        String payUrl = paymentService.initiatePayPal(participatingTeam.getParticipatingTeamId(), PaymentPayable.TOURNAMENT, (int) entryFee);

        Map<String, Object> response = new HashMap<>();
        response.put("payUrl", payUrl);
        response.put("teamId", participatingTeam.getTeam().getTeamId());

        return response;
    }

    public void confirmRegistration(Long teamId, String token, String payerId) {
        ParticipatingTeam participatingTeam = participatingTeamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new RuntimeException("Participating team not found"));
    }

    @Transactional
    public TournamentDto getTournamentBySlug(String slug) {
        TournamentDto tournamentDto = tournamentRepository.findBySlug(slug);
        if (tournamentDto != null) {
            List<ParticipatingTeam> participatingTeams = participatingTeamRepository.findByTournamentId(tournamentDto.getTournamentId());
            tournamentDto.setParticipatingTeams(participatingTeams.stream()
                    .map(pt -> teamRepository.findById(pt.getTeam().getTeamId()).orElse(null))
                    .filter(java.util.Objects::nonNull)
                    .map(teamMapper::toDto)
                    .collect(Collectors.toList()));
        }
        return tournamentDto;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTournamentReceipt(Long tournamentId) {
        // Get current authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get tournament details
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        // Find user's participation in this tournament
        List<ParticipatingTeam> userParticipations = participatingTeamRepository
                .findByTournamentId(tournamentId)
                .stream()
                .filter(pt -> {
                    // Check if user is captain or member of the team
                    return pt.getTeam().getTeamRosters().stream()
                            .anyMatch(roster -> roster.getUserId().equals(currentUser.getId()));
                })
                .collect(Collectors.toList());

        if (userParticipations.isEmpty()) {
            throw new RuntimeException("No participation found for this user in the tournament");
        }

        ParticipatingTeam participation = userParticipations.get(0);
        
        // Check if participation is confirmed (payment completed)
        if (!"CONFIRMED".equals(participation.getStatus())) {
            throw new RuntimeException("Tournament registration is not confirmed yet");
        }

        // Prepare tournament data
        TournamentDto tournamentDto = tournamentMapper.toDto(tournament);

        // Get captain information
        TeamRoster captain = participation.getTeam().getTeamRosters().stream()
                .filter(roster -> Boolean.TRUE.equals(roster.getIsCaptain()))
                .findFirst()
                .orElse(null);

        // Prepare participation data
        Map<String, Object> participationData = new HashMap<>();
        participationData.put("teamName", participation.getTeam().getName());
        if (captain != null && captain.getUser() != null) {
            participationData.put("captainName", captain.getUser().getFullName());
            participationData.put("contactPhone", captain.getUser().getPhoneNumber());
        }
        participationData.put("registrationDate", participation.getTeam().getCreatedAt());
        participationData.put("status", participation.getStatus());
        participationData.put("participationId", participation.getParticipatingTeamId());

        Map<String, Object> result = new HashMap<>();
        result.put("tournament", tournamentDto);
        result.put("participation", participationData);
        
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTournamentPublicReceipt(Long tournamentId) {
        // Get tournament details
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        // Find any participation in this tournament (for public access after PayPal callback)
        List<ParticipatingTeam> participations = participatingTeamRepository
                .findByTournamentId(tournamentId);

        if (participations.isEmpty()) {
            throw new RuntimeException("No participation found for this tournament");
        }

        // Get the most recent participation (or first one if multiple)
        ParticipatingTeam participation = participations.get(participations.size() - 1);
        
        // Allow both PENDING and CONFIRMED status for public access
        if (!"CONFIRMED".equals(participation.getStatus()) && !"PENDING".equals(participation.getStatus())) {
            throw new RuntimeException("Tournament registration status is invalid");
        }

        // Prepare tournament data
        TournamentDto tournamentDto = tournamentMapper.toDto(tournament);

        // Get captain information
        TeamRoster captain = participation.getTeam().getTeamRosters().stream()
                .filter(roster -> Boolean.TRUE.equals(roster.getIsCaptain()))
                .findFirst()
                .orElse(null);

        // Prepare participation data
        Map<String, Object> participationData = new HashMap<>();
        participationData.put("teamName", participation.getTeam().getName());
        if (captain != null && captain.getUser() != null) {
            participationData.put("captainName", captain.getUser().getFullName());
            participationData.put("contactPhone", captain.getUser().getPhoneNumber());
        }
        participationData.put("registrationDate", participation.getTeam().getCreatedAt());
        participationData.put("status", participation.getStatus());
        participationData.put("participationId", participation.getParticipatingTeamId());

        Map<String, Object> result = new HashMap<>();
        result.put("tournament", tournamentDto);
        result.put("participation", participationData);
        
        return result;
    }
}
