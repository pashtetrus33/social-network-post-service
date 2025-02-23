package ru.skillbox.social_network_post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skillbox.social_network_post.entity.Like;

import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByPostIdAndAuthorId(UUID postId, UUID userId);

    boolean existsByCommentIdAndAuthorId(UUID postId, UUID userId);
}