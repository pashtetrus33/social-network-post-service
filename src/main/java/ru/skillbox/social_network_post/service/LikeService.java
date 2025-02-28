package ru.skillbox.social_network_post.service;

import ru.skillbox.social_network_post.dto.LikeDto;;
import ru.skillbox.social_network_post.dto.PostReactionDTO;

import java.util.UUID;

public interface LikeService {
    PostReactionDTO addLikeToPost(UUID postId, LikeDto likeDto);

    void removeLikeFromPost(UUID postId);

    void addLikeToComment(UUID postId, UUID commentId);

    void removeLikeFromComment(UUID postId, UUID commentId);
}