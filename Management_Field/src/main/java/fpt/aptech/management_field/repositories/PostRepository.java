package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Post;
import fpt.aptech.management_field.payload.response.PostResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(value = "SELECT new fpt.aptech.management_field.payload.response.PostResponse(p.postId, p.title, p.content, p.imageUrl, null, p.category, p.user.username, p.user.profilePicture, size(p.comments), COALESCE((SELECT SUM(ul.likeCount) FROM PostLike ul WHERE ul.postId = p.postId), 0L), COALESCE((SELECT ul.likeCount FROM PostLike ul WHERE ul.postId = p.postId AND ul.userId = :userId), 0L)) FROM Post p",
            countQuery = "SELECT count(p) FROM Post p")
    List<PostResponse> getAllPosts(@Param("userId") Long userId);
}