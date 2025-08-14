package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private String createdAt;
    private String userName;
    private String userAvatar;
    private int userLikeStatus;
    private long likeCount;
    private List<CommentResponse> childComments = new ArrayList<>();
}