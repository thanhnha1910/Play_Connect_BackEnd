package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.PostLike;
import fpt.aptech.management_field.models.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    @Query(value = "SELECT pl FROM PostLike pl WHERE pl.userId = :userId AND pl.postId = :postId")
    PostLike findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);
}