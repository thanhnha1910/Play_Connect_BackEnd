package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "booking_recruitment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequirement {
      @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String userName;
    private String phone;
    private String fieldNumber;
    private String fieldLocation;
    private LocalDateTime playTime;
    private Integer peopleNeeded;
    private String message;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "recruitment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RecruitmentParticipant> participants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
