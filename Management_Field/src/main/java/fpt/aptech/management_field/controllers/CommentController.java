package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.models.Comment;
import fpt.aptech.management_field.payload.request.CommentRequest;
import fpt.aptech.management_field.payload.request.ReplyCommentRequest;
import fpt.aptech.management_field.payload.response.CommentResponse;
import fpt.aptech.management_field.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @GetMapping()
    public ResponseEntity<List<CommentResponse>> getCommentsForPost(@RequestParam Long postId, @RequestParam Long userId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId, userId));
    }

    @PostMapping()
    public ResponseEntity<Comment> createComment(@RequestBody CommentRequest commentRequest, @RequestParam Long postId, @RequestParam Long userId) {
        return ResponseEntity.ok(commentService.saveComment(commentRequest.getContent(), postId, userId));
    }

    @PutMapping("/like")
    public ResponseEntity<Void> likeComment(@RequestParam Long commentId, @RequestParam Long userId) {
        commentService.likeComment(commentId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reply")
    public ResponseEntity<CommentResponse> replyComment(@RequestBody ReplyCommentRequest replyCommentRequest, @RequestParam Long parentCommentId) {
        CommentResponse reply = commentService.replyToComment(parentCommentId, replyCommentRequest.getContent(), replyCommentRequest.getPostId(), replyCommentRequest.getUserId());
        return ResponseEntity.ok(reply);
    }
}