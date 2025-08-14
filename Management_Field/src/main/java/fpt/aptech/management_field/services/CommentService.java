package fpt.aptech.management_field.services;

import fpt.aptech.management_field.exception.ResourceNotFoundException;
import fpt.aptech.management_field.mappers.CommentMapper;
import fpt.aptech.management_field.models.Comment;
import fpt.aptech.management_field.models.CommentLike;
import fpt.aptech.management_field.models.Post;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.payload.response.CommentResponse;
import fpt.aptech.management_field.repositories.CommentLikeRepository;
import fpt.aptech.management_field.repositories.CommentRepository;
import fpt.aptech.management_field.repositories.PostRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentMapper commentMapper;

    public List<CommentResponse> getCommentsByPostId(Long postId, Long userId) {
        List<Comment> comments = commentRepository.findAllByPost_PostIdOrderByCreatedAtAsc(postId);

        Map<Long, CommentResponse> commentDtoMap = comments.stream()
                .collect(Collectors.toMap(Comment::getId, comment -> commentMapper.toDto(comment, userId)));

        comments.forEach(comment -> {
            if (comment.getParentComment() != null) {
                CommentResponse parentDto = commentDtoMap.get(comment.getParentComment().getId());
                if (parentDto != null) {
                    parentDto.getChildComments().add(commentDtoMap.get(comment.getId()));
                }
            }
        });

        return comments.stream()
                .filter(comment -> comment.getParentComment() == null)
                .map(comment -> commentDtoMap.get(comment.getId()))
                .collect(Collectors.toList());
    }

    public Comment saveComment(String content, Long postId, Long userId) {
        Comment comment = new Comment();
        comment.setContent(content);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post id: " + postId));
        comment.setPost(post);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User id: " + userId));
        comment.setUser(user);

        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public void likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment id: " + commentId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User id: " + userId));

        Optional<CommentLike> existingLike = commentLikeRepository.findByUserAndComment(user, comment);

        if (existingLike.isPresent()) {
            CommentLike commentLike = existingLike.get();
            commentLike.setLikeCount(commentLike.getLikeCount() == 1 ? 0 : 1);
            commentLikeRepository.save(commentLike);
        } else {
            CommentLike newLike = new CommentLike();
            newLike.setUser(user);
            newLike.setComment(comment);
            newLike.setLikeCount(1);
            commentLikeRepository.save(newLike);
        }
    }

    public CommentResponse replyToComment(Long parentCommentId, String content, Long postId, Long userId) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent comment id: " + parentCommentId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post id: " + postId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User id: " + userId));

        Comment replyComment = new Comment();
        replyComment.setContent(content);
        replyComment.setPost(post);
        replyComment.setUser(user);
        replyComment.setParentComment(parentComment);
        replyComment.setCreatedAt(LocalDateTime.now());

        Comment savedReply = commentRepository.save(replyComment);
        return commentMapper.toDto(savedReply, userId);
    }

    public CommentResponse getCommentById(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment id: " + commentId));
        return commentMapper.toDto(comment, userId);
    }
}