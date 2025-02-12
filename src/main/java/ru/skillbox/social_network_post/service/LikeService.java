package ru.skillbox.social_network_post.service;

import java.util.UUID;

public interface LikeService {
    void addLikeToPost(UUID postId);

    void removeLikeFromPost(UUID postId);

    void addLikeToComment(UUID postId, UUID commentId);

    void removeLikeFromComment(UUID postId, UUID commentId);
}