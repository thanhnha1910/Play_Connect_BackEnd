package fpt.aptech.management_field.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private String createdAt;
    private String category;
    private String userName;
    private String userAvatar;
    private int commentCount;
    private long likeCount;
    private int userLikeStatus;
}