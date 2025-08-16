package fpt.aptech.management_field.controllers;

import fpt.aptech.management_field.payload.dtos.FieldReviewListItemDTO;
import fpt.aptech.management_field.payload.request.ReplyCommentRequest;
import fpt.aptech.management_field.payload.response.CommentResponse;
import fpt.aptech.management_field.services.CommentService;
import fpt.aptech.management_field.services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Review Management", description = "Lọc đánh giá theo sân/địa điểm + phản hồi đánh giá")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CommentService commentService;

    @GetMapping("/fields")
    @Operation(summary = "Danh sách đánh giá sân (lọc + phân trang)")
    public ResponseEntity<Page<FieldReviewListItemDTO>> listFieldReviews(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long fieldId,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal maxRating,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction dir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<FieldReviewListItemDTO> data = reviewService.searchFieldReviews(
                locationId, fieldId, minRating, maxRating, fromDate, toDate, q, sortBy, dir, page, size
        );
        return ResponseEntity.ok(data);
    }


    @PostMapping("/fields/{reviewId}/reply")
    @Operation(summary = "Phản hồi đánh giá sân (proxy)")
    public ResponseEntity<CommentResponse> replyToFieldReview(
            @PathVariable Long reviewId,
            @RequestParam Long parentCommentId,
            @RequestBody ReplyCommentRequest body
    ) {
        CommentResponse res = commentService.replyToComment(
                parentCommentId,
                body.getContent(),
                body.getPostId(),
                body.getUserId()
        );
        return ResponseEntity.ok(res);
    }


    // PUT /api/reviews/fields/{reviewId}/owner-reply
    @PutMapping("/fields/{reviewId}/owner-reply")
    public ResponseEntity<FieldReviewListItemDTO> updateOwnerReply(
            @PathVariable Long reviewId,
            @RequestBody fpt.aptech.management_field.payload.request.OwnerReplyRequest body
    ) {
        FieldReviewListItemDTO dto = reviewService.ownerReply(reviewId, body.getContent());
        return ResponseEntity.ok(dto);
    }
}
