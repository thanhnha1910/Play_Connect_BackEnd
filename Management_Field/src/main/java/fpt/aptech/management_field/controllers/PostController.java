package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.exception.ResourceNotFoundException;
import fpt.aptech.management_field.models.Post;
import fpt.aptech.management_field.models.User;
import fpt.aptech.management_field.models.PostLike;
import fpt.aptech.management_field.payload.response.PostResponse;
import fpt.aptech.management_field.repositories.PostRepository;
import fpt.aptech.management_field.repositories.PostLikeRepository;
import fpt.aptech.management_field.repositories.UserRepository;
import fpt.aptech.management_field.services.CommentService;
import fpt.aptech.management_field.services.FileStorageService;
import fpt.aptech.management_field.services.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private ImageStorageService imageStorageService;

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(@RequestParam(required = false) Long userId) {
        List<PostResponse> posts = postRepository.getAllPosts(userId);
        return ResponseEntity.ok(posts);
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestParam("title") String title,
                                           @RequestParam("content") String content,
                                           @RequestParam(value = "category", required = false) String category, // Assuming category is optional
                                           @RequestParam("userId") Long userId,
                                           @RequestParam(value = "image", required = false) MultipartFile file) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setCategory(category);
        post.setUser(user);

        if (file != null && !file.isEmpty()) {
            try {
                String imageUrl = imageStorageService.uploadSingleImage(file, ImageStorageService.ImageType.POST_COMMUNITY);
                post.setImageUrl(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Error uploading image: " + e.getMessage());
            }
        }

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

    
