package ru.skillbox.social_network_post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.skillbox.social_network_post.entity.Post;

import java.util.List;
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
    @Query("UPDATE Post p SET p.likeAmount = p.likeAmount + 1, p.myLike = true WHERE p.id = :postId")
    void incrementLikeAmountAndSetMyLike(@Param("postId") UUID postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.likeAmount = p.likeAmount + 1 WHERE p.id = :postId")
    void incrementLikeAmount(@Param("postId") UUID postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.likeAmount = p.likeAmount - 1 WHERE p.id = :postId")
    void updateLikeAmount(@Param("postId") UUID postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.likeAmount = p.likeAmount - 1, p.myLike = false WHERE p.id = :postId AND p.myLike = true")
    void updateLikeAmountAndUnsetMyLike(UUID postId);

    @Query("SELECT p.likeAmount FROM Post p WHERE p.id = :postId")
    int getLikeAmount(@Param("postId") UUID postId);

    @Query("SELECT COUNT(p) > 0 FROM Post p WHERE p.id = :postId AND p.authorId = :userId")
    boolean isAuthorOfPost(@Param("postId") UUID postId, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE Post p SET p.isBlocked = true WHERE p.authorId = :uuid")
    void updateBlockedStatusForAccount(UUID uuid);

    @Modifying
    @Query("UPDATE Post p SET p.isDeleted = true WHERE p.authorId = :uuid")
    void updateDeletedStatusForAccount(UUID uuid);
}