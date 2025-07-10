package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.exception.ResourceNotFoundException;
import fpt.aptech.management_field.models.Post;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.models.PostLike;
import fpt.aptech.management_field.payload.request.PostRequest;
import fpt.aptech.management_field.payload.response.PostResponse;
import fpt.aptech.management_field.repositories.PostRepository;
import fpt.aptech.management_field.repositories.PostLikeRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import fpt.aptech.management_field.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private CommentService commentService;

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(@RequestParam(required = false) Long userId) {
        List<PostResponse> posts = postRepository.getAllPosts(userId);
        return ResponseEntity.ok(posts);
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody PostRequest postRequest) {
        User user = userRepository.findById(postRequest.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setCategory(postRequest.getCategory());
        post.setImageUrl(postRequest.getImageUrl());
        post.setUser(user);
        Post savedPost = postRepository.save(post);
        return ResponseEntity.ok(savedPost);
    }

    @PutMapping("/like")
    public ResponseEntity<PostLike> likePost(@RequestParam Long postId, @RequestParam Long userId) {
        PostLike postLike = postLikeRepository.findByUserIdAndPostId(userId, postId);
        if (postLike == null) {
            postLike = new PostLike();
            postLike.setPostId(postId);
            postLike.setUserId(userId);
            postLike.setLikeCount(1);
            postLikeRepository.save(postLike);
            return ResponseEntity.ok(postLike);
        }
        
        if (postLike.getLikeCount() == 1) {
            postLike.setLikeCount(0);
        } else {
            postLike.setLikeCount(1);
        }
        postLikeRepository.save(postLike);
        return ResponseEntity.ok(postLike);
    }
}

