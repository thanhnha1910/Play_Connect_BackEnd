package fpt.aptech.management_field.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamRosterId implements Serializable {
    private Long teamId;
    private Long userId;
}