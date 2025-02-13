package ru.skillbox.social_network_post.web.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ru.skillbox.social_network_post.entity.PostType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    @NotNull(message = "Id must not be null")
    private UUID id;

    @NotNull(message = "Time must not be null")
    @PastOrPresent(message = "Time must be in the past or present")
    private LocalDateTime time;


    @NotNull(message = "TimeChanged must not be null")
    @PastOrPresent(message = "TimeChanged must be in the past or present")
    private LocalDateTime timeChanged;

    @NotNull
    private UUID authorId;

    @NotNull(message = "Title must not be null")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotNull(message = "Post type must not be null")
    private PostType type;

    @NotNull(message = "Post text must not be null")
    private String postText;

    @NotNull(message = "Blocked status must not be null")
    private Boolean isBlocked;

    @NotNull(message = "Deleted status must not be null")
    private Boolean isDeleted;

    @NotNull(message = "Comments count must not be null")
    @Min(value = 0, message = "Comments count must be at least 0")
    private Integer commentsCount;

    @Size(max = 50, message = "Tags list size must not exceed 50")
    private List<String> tags;

    @NotNull(message = "Like amount must not be null")
    @Min(value = 0, message = "Like amount must be at least 0")
    private Integer likeAmount;

    @NotNull(message = "My like status must not be null")
    private Boolean myLike = false;

    @Size(max = 512, message = "Image path must not exceed 512 characters")
    private String imagePath;

    @NotNull(message = "Publish date must not be null")
    @PastOrPresent(message = "Publish date must be in the past or present")
    private LocalDateTime publishDate;
}
