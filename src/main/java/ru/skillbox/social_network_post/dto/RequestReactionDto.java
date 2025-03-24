package ru.skillbox.social_network_post.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;


@Builder
public record RequestReactionDto(@NotNull(message = "Type must not be null") String type,
                                 @NotNull(message = "ReactionType must not be null") String reactionType) {

}