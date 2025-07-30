package fpt.aptech.management_field.services;

import fpt.aptech.management_field.models.ParticipatingTeam;

import fpt.aptech.management_field.models.Team;
import fpt.aptech.management_field.models.Tournament;
import fpt.aptech.management_field.payload.dtos.LocationDto;
import fpt.aptech.management_field.payload.dtos.TournamentDto;
import fpt.aptech.management_field.payload.request.TournamentRegistrationRequest;
import fpt.aptech.management_field.repositories.LocationRepository;
import fpt.aptech.management_field.repositories.ParticipatingTeamRepository;
import fpt.aptech.management_field.repositories.TeamRepository;
import fpt.aptech.management_field.repositories.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private LocationRepository locationRepository;

    @Autowired
    private ParticipatingTeamRepository participatingTeamRepository;

    @Autowired
    private PayPalPaymentService payPalPaymentService;

    @Autowired
    private TournamentMapper tournamentMapper;

    @Autowired
    private TeamMapper teamMapper;

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
        participatingTeam.setTeamId(team.getTeamId());
        participatingTeam.setTournamentId(tournament.getTournamentId());
        participatingTeam.setStatus("pending");
        participatingTeamRepository.save(participatingTeam);

        double entryFee = tournament.getEntryFee();
        String payUrl = payPalPaymentService.initiatePayPalPayment(participatingTeam.getTeamId(), (float) entryFee);

        Map<String, Object> response = new HashMap<>();
        response.put("payUrl", payUrl);
        response.put("teamId", participatingTeam.getTeamId());

        return response;
    }

    public void confirmRegistration(Long teamId, String token, String payerId) {
        ParticipatingTeam participatingTeam = participatingTeamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new RuntimeException("Participating team not found"));

        if (payPalPaymentService.verifyPaymentWithPayPal(token, payerId)) {
            participatingTeam.setStatus("confirmed");
            participatingTeam.setPaymentToken(token);
            participatingTeamRepository.save(participatingTeam);
        } else {
            throw new RuntimeException("Payment verification failed");
        }
    }

    @Transactional
    public TournamentDto getTournamentBySlug(String slug) {
        TournamentDto tournamentDto = tournamentRepository.findBySlug(slug);
        if (tournamentDto != null) {
            List<ParticipatingTeam> participatingTeams = participatingTeamRepository.findByTournamentId(tournamentDto.getTournamentId());
            tournamentDto.setParticipatingTeams(participatingTeams.stream()
                    .map(pt -> teamRepository.findById(pt.getTeamId()).orElse(null))
                    .filter(java.util.Objects::nonNull)
                    .map(teamMapper::toDto)
                    .collect(Collectors.toList()));
        }
        return tournamentDto;
    }
}
