package ru.skillbox.social_network_post.dto;


import jakarta.validation.constraints.NotNull;

public record LikeDto(
        @NotNull(message = "Type must not be null") String type,
        @NotNull(message = "ReactionType must not be null") String reactionType) {
}