package ru.skillbox.social_network_post.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ru.skillbox.social_network_post.entity.CommentType;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private UUID id;

    private CommentType commentType;

    private LocalDateTime time;

    private LocalDateTime timeChanged;

    private UUID authorId;

    private UUID parentId;

    @NotBlank(message = "Comment text must not be blank")
    private String commentText;

    private UUID postId;

    private Boolean isBlocked;

    private Boolean isDeleted;

    private Integer likeAmount;

    private Boolean myLike;

    private Integer commentsCount;

    @Size(max = 512, message = "Image path must not exceed 512 characters")
    private String imagePath;
}