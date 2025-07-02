package fpt.aptech.management_field.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class BookingRequest {
    @NotNull(message = "Field ID is required")
    private Long fieldId;

    @NotNull(message = "From time is required")
    private Instant fromTime;

    @NotNull(message = "To time is required")
    private Instant toTime;

    private Integer slots;

    private boolean findTeammates;

    private List<AdditionalPlayer> additionalPlayers;

    @Data
    public static class AdditionalPlayer {
        @NotNull(message = "User ID is required")
        private Long userId;

        private String position;
    }
}