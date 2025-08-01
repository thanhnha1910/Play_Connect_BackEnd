package fpt.aptech.management_field.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvertDraftToMatchRequest {
    private Long fieldId;
    private Instant startTime;
    private Instant endTime;
    private String notes;
}