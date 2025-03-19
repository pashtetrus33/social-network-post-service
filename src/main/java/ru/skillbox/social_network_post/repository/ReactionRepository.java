package ru.skillbox.social_network_post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skillbox.social_network_post.entity.Reaction;

import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {


    boolean existsByPostIdAndAuthorIdAndCommentIdIsNull(UUID postId, UUID userId);

    boolean existsByCommentIdAndAuthorId(UUID postId, UUID userId);

    void deleteByPostIdAndAuthorId(UUID postId, UUID userId);

    void deleteByCommentIdAndAuthorId(UUID commentId, UUID userId);

    long countByCommentIdAndReactionType(UUID commentId, String reactionType);
}