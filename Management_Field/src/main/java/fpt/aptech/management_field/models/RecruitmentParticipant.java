package fpt.aptech.management_field.models;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recruitment_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    @Column(name = "is_accepted")
    private Boolean isAccepted;

    @Column(name = "number_of_people")
    private Integer numberOfPeople;

    @ManyToOne
    @JoinColumn(name = "recruitment_id")
    private BookingRequirement recruitment;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
