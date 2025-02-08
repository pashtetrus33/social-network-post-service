package ru.skillbox.social_network_post.service;

public interface LikeService {
    void addLikeToPost(Long postId);

    void removeLikeFromPost(Long postId);

    void addLikeToComment(Long postId, Long commentId);

    void removeLikeFromComment(Long postId, Long commentId);
}
