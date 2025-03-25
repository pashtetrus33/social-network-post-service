package ru.skillbox.social_network_post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.skillbox.social_network_post.entity.Reaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {


    boolean existsByPostIdAndAuthorIdAndCommentIdIsNull(UUID postId, UUID userId);

    boolean existsByCommentIdAndAuthorId(UUID postId, UUID userId);

    void deleteByPostIdAndAuthorId(UUID postId, UUID userId);

    void deleteByCommentIdAndAuthorId(UUID commentId, UUID userId);

    long countByPostId(UUID postId);

    @Query("SELECT r.reactionType, COUNT(r) FROM Reaction r WHERE r.post.id = :postId GROUP BY r.reactionType")
    List<Object[]> countReactionsByPostId(@Param("postId") UUID postId);

    Optional<Reaction> findByPostIdAndAuthorId(UUID postId, UUID authorId);
}