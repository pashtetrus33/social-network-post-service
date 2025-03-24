package ru.skillbox.social_network_post.service;

import ru.skillbox.social_network_post.dto.ReactionDto;
import ru.skillbox.social_network_post.dto.RequestReactionDto;

import java.util.UUID;

public interface ReactionService {
    ReactionDto addLikeToPost(UUID postId, RequestReactionDto requestReactionDto);

    void removeLikeFromPost(UUID postId);

    void addLikeToComment(UUID postId, UUID commentId);

    void removeLikeFromComment(UUID postId, UUID commentId);
}