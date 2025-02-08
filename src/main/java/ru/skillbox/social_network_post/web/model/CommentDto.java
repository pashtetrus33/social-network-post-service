package ru.skillbox.social_network_post.web.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ru.skillbox.social_network_post.entity.CommentType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    @NotNull
    @Positive
    private Long id;

    @NotNull
    private CommentType commentType;

    @NotNull
    @PastOrPresent
    private LocalDateTime time;

    @NotNull
    @PastOrPresent
    private LocalDateTime timeChanged;

    @NotNull
    @Positive
    private Long authorId;

    @Positive
    private Long parentId;

    @NotBlank
    private String commentText;

    @NotNull
    @Positive
    private Long postId;

    @NotNull
    private Boolean isBlocked;

    @NotNull
    private Boolean isDelete;

    @NotNull
    @Min(0)
    private Integer likeAmount;

    @NotNull
    private Boolean myLike;

    @NotNull
    @Min(0)
    private Integer commentsCount;

    @Size(max = 512)
    private String imagePath;
}