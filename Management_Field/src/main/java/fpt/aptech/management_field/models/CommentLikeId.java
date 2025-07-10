package fpt.aptech.management_field.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentLikeId implements Serializable {
    private Long userId;
    private Long commentId;
}
