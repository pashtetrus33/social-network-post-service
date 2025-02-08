package ru.skillbox.social_network_post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skillbox.social_network_post.entity.Comment;


import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostId(Long postId, Pageable pageable);

    void deleteByIdAndPostId(Long commentId, Long postId);

    Page<Comment> findByParentCommentIdAndPostId(Long commentId, Long postId, Pageable pageable);

    Optional<Comment> findByIdAndPostId(Long commentId, Long postId);

    boolean existsByIdAndPostId(Long commentId, Long postId);
}