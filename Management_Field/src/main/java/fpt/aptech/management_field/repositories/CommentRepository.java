package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Comment;
import fpt.aptech.management_field.payload.response.CommentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPost_PostIdOrderByCreatedAtAsc(Long postId);
}