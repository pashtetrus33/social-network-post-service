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

    private UUID id;

    private LocalDateTime time;

    private LocalDateTime timeChanged;

    private UUID authorId;

    @NotNull(message = "Title must not be null")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private PostType type;

    @NotNull(message = "Post text must not be null")
    private String postText;

    private Boolean isBlocked;

    private Boolean isDeleted;

    private Integer commentsCount;

    @Size(max = 50, message = "Tags list size must not exceed 50")
    private List<String> tags;

    private Integer likeAmount;

    private Boolean myLike = false;

    @Size(max = 512, message = "Image path must not exceed 512 characters")
    private String imagePath;

    private LocalDateTime publishDate;
}
