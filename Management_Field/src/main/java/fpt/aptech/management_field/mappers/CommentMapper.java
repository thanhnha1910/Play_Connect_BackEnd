package fpt.aptech.management_field.mappers;

import fpt.aptech.management_field.models.Comment;
import fpt.aptech.management_field.models.CommentLike;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.response.CommentResponse;
import fpt.aptech.management_field.repositories.CommentLikeRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class CommentMapper {

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private UserRepository userRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CommentResponse toDto(Comment comment, Long userId) {
        if (comment == null) {
            return null;
        }

        CommentResponse dto = new CommentResponse();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        if (comment.getCreatedAt() != null) {
            dto.setCreatedAt(comment.getCreatedAt().format(formatter));
        }
        if (comment.getUser() != null) {
            dto.setUserName(comment.getUser().getUsername());
            dto.setUserAvatar(comment.getUser().getImageUrl());
        }

        // Set userLikeStatus
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                Optional<CommentLike> commentLike = commentLikeRepository.findByUserAndComment(user, comment);
                dto.setUserLikeStatus(commentLike.map(CommentLike::getLikeCount).orElse(0));
            }
        }

        return dto;
    }
}