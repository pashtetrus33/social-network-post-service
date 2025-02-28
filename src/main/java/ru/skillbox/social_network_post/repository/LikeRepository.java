package ru.skillbox.social_network_post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skillbox.social_network_post.entity.Reaction;

import java.util.UUID;

public interface LikeRepository extends JpaRepository<Reaction, Long> {

    boolean existsByPostIdAndAuthorId(UUID postId, UUID userId);

    boolean existsByCommentIdAndAuthorId(UUID postId, UUID userId);

    void deleteByPostIdAndAuthorId(UUID postId, UUID userId);

    void deleteByCommentIdAndAuthorId(UUID commentId, UUID userId);
}