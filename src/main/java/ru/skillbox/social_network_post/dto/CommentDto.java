package ru.skillbox.social_network_post.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ru.skillbox.social_network_post.entity.CommentType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    @NotNull(message = "ID must not be null")
    private UUID id;

    @NotNull(message = "Comment type must not be null")
    private CommentType commentType;

    @NotNull(message = "Time must not be null")
    @PastOrPresent(message = "Time must be in the past or present")
    private LocalDateTime time;

    @NotNull(message = "Time changed must not be null")
    @PastOrPresent(message = "Time changed must be in the past or present")
    private LocalDateTime timeChanged;

    @NotNull(message = "Author ID must not be null")
    @Positive(message = "Author ID must be a positive number")
    private Long authorId;

    private UUID parentId;

    @NotBlank(message = "Comment text must not be blank")
    private String commentText;

    @NotNull(message = "Post ID must not be null")
    private UUID postId;

    @NotNull(message = "Blocked status must not be null")
    private Boolean isBlocked;

    @NotNull(message = "Delete status must not be null")
    private Boolean isDelete;

    @NotNull(message = "Like amount must not be null")
    @Min(value = 0, message = "Like amount must be at least 0")
    private Integer likeAmount;

    @NotNull(message = "My like status must not be null")
    private Boolean myLike;

    @NotNull(message = "Comments count must not be null")
    @Min(value = 0, message = "Comments count must be at least 0")
    private Integer commentsCount;

    @Size(max = 512, message = "Image path must not exceed 512 characters")
    private String imagePath;
}