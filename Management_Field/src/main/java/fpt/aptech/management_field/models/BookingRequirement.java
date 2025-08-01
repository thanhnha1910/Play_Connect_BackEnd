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

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String userName;
    private String phone;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String fieldNumber;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String fieldLocation;
    private LocalDateTime playTime;
    private Integer peopleNeeded;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String message;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "recruitment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RecruitmentParticipant> participants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // Explicit getters for compatibility
    public String getUserName() {
        return userName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public String getFieldNumber() {
        return fieldNumber;
    }
    
    public String getFieldLocation() {
        return fieldLocation;
    }
    
    public LocalDateTime getPlayTime() {
        return playTime;
    }
    
    public Integer getPeopleNeeded() {
        return peopleNeeded;
    }
    
    public String getMessage() {
        return message;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public List<RecruitmentParticipant> getParticipants() {
        return participants;
    }
}
