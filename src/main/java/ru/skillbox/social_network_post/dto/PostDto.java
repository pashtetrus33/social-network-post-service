package ru.skillbox.social_network_post.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
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

    @Builder.Default
    private Boolean isBlocked = false;

    @Builder.Default
    private Boolean isDeleted = false;

    @Builder.Default
    private Long commentsCount = 0L;

    @Size(max = 50, message = "Tags list size must not exceed 50")
    private List<String> tags;

    @Builder.Default
    private Long likeAmount = 0L;

    @Builder.Default
    private List<ReactionDto.ReactionInfo> reactionType = List.of(
            ReactionDto.ReactionInfo.builder().reactionType(String.valueOf(LikeType.delight)).count(0L).build(),
            ReactionDto.ReactionInfo.builder().reactionType(String.valueOf(LikeType.malice)).count(0L).build(),
            ReactionDto.ReactionInfo.builder().reactionType(String.valueOf(LikeType.heart)).count(0L).build(),
            ReactionDto.ReactionInfo.builder().reactionType(String.valueOf(LikeType.sadness)).count(0L).build(),
            ReactionDto.ReactionInfo.builder().reactionType(String.valueOf(LikeType.funny)).count(0L).build(),
            ReactionDto.ReactionInfo.builder().reactionType(String.valueOf(LikeType.wow)).count(0L).build()
    );

    private String myReaction;

    @Builder.Default
    private Boolean myLike = false;

    @Size(max = 512, message = "Image path must not exceed 512 characters")
    private String imagePath;

    private LocalDateTime publishDate;
}

