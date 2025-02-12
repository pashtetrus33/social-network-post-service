package ru.skillbox.social_network_post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.skillbox.social_network_post.entity.Comment;

import java.util.UUID;


@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findByPostId(UUID postId, Pageable pageable);

    Page<Comment> findByParentCommentIdAndPostId(UUID commentId, UUID postId, Pageable pageable);

    void deleteByPostId(UUID postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.parentComment.id = :parentId")
    void deleteAllByParentId(@Param("parentId") UUID parentId);
}