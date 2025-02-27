package ru.skillbox.social_network_post.dto;

import java.util.List;

public record LikeResponseDTO(boolean success, List<ReactionDTO> reactions) {
    public record ReactionDTO(String reactionType, int likesCount) {
    }
}
