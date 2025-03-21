package ru.skillbox.social_network_post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReactionDto {
    @NotNull(message = "Type must not be null")
    private final String type;

    @NotNull(message = "ReactionType must not be null")
    private final String reactionType;

    @Builder.Default
    private Long count = 0L;
}