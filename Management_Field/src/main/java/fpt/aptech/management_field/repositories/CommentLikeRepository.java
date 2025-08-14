package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.CommentLike;
import fpt.aptech.management_field.models.Comment;
import fpt.aptech.management_field.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);
    
    @Query("SELECT SUM(cl.likeCount) FROM CommentLike cl WHERE cl.comment = :comment")
    Long countLikesByComment(@Param("comment") Comment comment);
}