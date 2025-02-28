package ru.skillbox.social_network_post.service;

import ru.skillbox.social_network_post.dto.ReactionDto;

import java.util.UUID;

public interface LikeService {
    ReactionDto addLikeToPost(UUID postId, ReactionDto reactionDto);

    void removeLikeFromPost(UUID postId);

    void addLikeToComment(UUID postId, UUID commentId);

    void removeLikeFromComment(UUID postId, UUID commentId);
}