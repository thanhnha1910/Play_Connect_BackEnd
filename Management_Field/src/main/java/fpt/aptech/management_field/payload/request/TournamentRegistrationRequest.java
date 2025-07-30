package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TournamentRegistrationRequest {
    @NotNull(message = "Team ID is required")
    private Long teamId;

    @NotNull(message = "Tournament ID is required")
    private Long tournamentId;
}