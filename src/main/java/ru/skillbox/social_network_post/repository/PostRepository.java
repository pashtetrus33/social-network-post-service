package ru.skillbox.social_network_post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.skillbox.social_network_post.entity.Post;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID>, JpaSpecificationExecutor<Post> {

    @EntityGraph(attributePaths = {"comments", "tags"})
    Optional<Post> findById(UUID postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.isDeleted = true WHERE p.id = :postId")
    void markAsDeleted(@Param("postId") UUID postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.reactionsCount = p.reactionsCount + 1, p.myReaction = true WHERE p.id = :postId")
    void incrementReactionsCountAndSetMyReaction(@Param("postId") UUID postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.reactionsCount = p.reactionsCount + 1 WHERE p.id = :postId")
    void incrementReactionsCount(@Param("postId") UUID postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.reactionsCount = p.reactionsCount - 1 WHERE p.id = :postId")
    void updateReactionsCount(@Param("postId") UUID postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.reactionsCount = p.reactionsCount - 1, p.myReaction = false WHERE p.id = :postId AND p.myReaction = true")
    void updateReactionsCountAndUnsetMyReaction(UUID postId);

    @Query("SELECT p.reactionsCount FROM Post p WHERE p.id = :postId")
    int getReactionsCount(@Param("postId") UUID postId);

    @Query("SELECT COUNT(p) > 0 FROM Post p WHERE p.id = :postId AND p.authorId = :userId")
    boolean isAuthorOfPost(@Param("postId") UUID postId, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Post p SET p.isBlocked = true WHERE p.authorId = :uuid")
    void updateBlockedStatusForAccount(UUID uuid);

    @Modifying
    @Query("UPDATE Post p SET p.isDeleted = true WHERE p.authorId = :uuid")
    void updateDeletedStatusForAccount(UUID uuid);
}