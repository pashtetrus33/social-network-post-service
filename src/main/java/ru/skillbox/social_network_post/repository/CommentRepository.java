package ru.skillbox.social_network_post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.social_network_post.entity.Comment;

import java.util.UUID;


@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID>, JpaSpecificationExecutor<Comment> {

    Page<Comment> findByParentCommentIdAndPostId(UUID commentId, UUID postId, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = true WHERE c.post.id = :postId")
    void markAllAsDeletedByPostId(@Param("postId") UUID postId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = true WHERE c.post.id = :postId AND c.id = :commentId")
    void markCommentAsDeletedByPostIdAndCommentId(@Param("postId")UUID postId, @Param("commentId") UUID commentId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.likeAmount = c.likeAmount + 1, c.myLike = true WHERE c.id = :commentId")
    void incrementLikeAmountAndSetMyLike(@Param("commentId") UUID commentId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.likeAmount = c.likeAmount + 1 WHERE c.id = :commentId")
    void incrementLikeAmount(@Param("commentId") UUID commentId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.likeAmount = c.likeAmount - 1 WHERE c.id = :commentId")
    void updateLikeAmount(@Param("commentId") UUID commentId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.likeAmount = c.likeAmount - 1, c.myLike = false WHERE c.id = :commentId AND c.myLike = true")
    void updateLikeAmountAndUnsetMyLike(@Param("commentId") UUID commentId);

    @Query("SELECT c.likeAmount FROM Comment c WHERE c.id = :commentId")
    int getLikeAmount(@Param("commentId") UUID commentId);

    @Query("SELECT COUNT(c) > 0 FROM Comment c WHERE c.id = :commentId AND c.authorId = :userId")
    boolean isAuthorOfComment(@Param("commentId") UUID commentId, @Param("userId") UUID userId);

    boolean existsByPostIdAndId(UUID postId, UUID commentId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.commentsCount = c.commentsCount + 1 WHERE c.id = :parentId")
    void incrementCommentsAmount(@Param("parentId") UUID parentId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.commentsCount = c.commentsCount - 1 WHERE c.id = :parentId")
    void decrementCommentsAmount(@Param("parentId") UUID parentId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = true WHERE c.parentComment.id = :commentId")
    void markAllAsDeletedByParentComment(@Param("commentId") UUID commentId);
}