package ru.skillbox.social_network_post.dto;

import lombok.*;
import ru.skillbox.social_network_post.entity.CommentType;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentSearchDto {
    private UUID postId;
    private UUID parentCommentId;
    private Long likeAmount;
    private Long commentsCount;
    private Boolean isBlocked;
    private Boolean isDeleted;
    private Boolean myLike;
    private String commentText;
    private String imagePath;
    private CommentType commentType;
}