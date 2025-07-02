package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "friends")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(FriendId.class)
public class Friend {
    @Id
    @Column(name = "user_id1")
    private Long userId1;
    
    @Id
    @Column(name = "user_id2")
    private Long userId2;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne
    @JoinColumn(name = "user_id1", insertable = false, updatable = false)
    private User user1;
    
    @ManyToOne
    @JoinColumn(name = "user_id2", insertable = false, updatable = false)
    private User user2;
}