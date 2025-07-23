package fpt.aptech.management_field.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_likes")
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(PostLikeId.class)
public class PostLike {
    @Id
    private Long postId;

    @Id
    private Long userId;

    @Column(name = "like_count")
    private int likeCount;
}