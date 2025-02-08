package ru.skillbox.social_network_post.web.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ru.skillbox.social_network_post.entity.PostType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    @NotNull
    @Positive
    private Long id;

    @NotNull
    @PastOrPresent
    private LocalDateTime time;

    @NotNull
    @PastOrPresent
    private LocalDateTime timeChanged;

    @NotNull
    @Positive
    private Long authorId;

    @NotNull
    @Size(max = 255)
    private String title;

    @NotNull
    private PostType type;

    @NotNull
    private String postText;

    @NotNull
    private Boolean isBlocked;

    @NotNull
    private Boolean isDelete;

    @NotNull
    @Min(0)
    private Integer commentsCount;

    @Size(max = 50)
    private List<String> tags;

    @NotNull
    @Min(0)
    private Integer likeAmount;

    @NotNull
    private Boolean myLike = false;

    @Size(max = 512)
    private String imagePath;

    @NotNull
    @PastOrPresent
    private LocalDateTime publishDate;
}
