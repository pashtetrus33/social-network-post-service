package ru.skillbox.social_network_post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.exception.CommentNotFoundException;
import ru.skillbox.social_network_post.exception.NegativeLikeCountException;
import ru.skillbox.social_network_post.exception.PostNotFoundException;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.service.LikeService;

import java.text.MessageFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public void addLikeToPost(Long postId) {
        log.info("Adding like to post with id: {}", postId);
        Post post = checkPostPresence(postId);

        if (post.getMyLike()) {
            throw new IllegalStateException(
                    MessageFormat.format("Like already exists for post with id {0}", postId));
        }

        post.setLikeAmount(post.getLikeAmount() + 1);
        post.setMyLike(true);
        postRepository.save(post);
        log.info("Like added to post with id: {}", postId);
    }


    @Override
    @Transactional
    public void removeLikeFromPost(Long postId) {

        Post post = checkPostPresence(postId);

        if (!post.getMyLike()) {
            throw new IllegalStateException(
                    MessageFormat.format("User cannot remove like from post {0} because he has not liked it", postId));
        }

        if (post.getLikeAmount() - 1 < 0) {
            throw new NegativeLikeCountException(
                    MessageFormat.format("Cannot remove like from post {0}: like count cannot be negative", postId));
        }
        post.setLikeAmount(post.getLikeAmount() - 1);
        post.setMyLike(false);
        postRepository.save(post);
        log.info("Like removed from post with id: {}", postId);
    }

    @Override
    @Transactional
    public void addLikeToComment(Long postId, Long commentId) {
        log.info("Adding like to comment with id: {} on post id: {}", commentId, postId);

        Comment comment = checkCommentAndPostPresence(postId, commentId);

        if (comment.getMyLike()) {
            throw new IllegalStateException(
                    MessageFormat.format(
                            "Like already exists for post with id {0} and comment with id {1}", postId, commentId));
        }

        comment.setLikeAmount(comment.getLikeAmount() + 1);
        comment.setMyLike(true);
        commentRepository.save(comment);
        log.info("Like added to comment with id: {}", commentId);

    }


    @Override
    @Transactional
    public void removeLikeFromComment(Long postId, Long commentId) {
        log.info("Removing like from comment with id: {} on post id: {}", commentId, postId);

        Comment comment = checkCommentAndPostPresence(postId, commentId);

        if (!comment.getMyLike()) {
            throw new IllegalStateException(
                    MessageFormat.format(
                            "User cannot remove like from post {0} and comment {1} because he has not liked it", postId, commentId));
        }

        if (comment.getLikeAmount() - 1 < 0) {
            throw new NegativeLikeCountException(
                    MessageFormat.format(
                            "Cannot remove like from post {0} and comment id {1}: like count cannot be negative", postId, commentId));
        }

        comment.setLikeAmount(comment.getLikeAmount() - 1);
        comment.setMyLike(false);
        commentRepository.save(comment);
        log.info("Like removed from comment with id: {}", commentId);

    }

    private Post checkPostPresence(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException(MessageFormat.format("Post with id {0} not found", postId)));
    }

    private Comment checkCommentPresence(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new CommentNotFoundException(MessageFormat.format("Comment with id {0} not found", commentId)));
    }

    private Comment checkCommentAndPostPresence(Long postId, Long commentId) {

        checkPostPresence(postId);
        Comment comment = checkCommentPresence(commentId);

        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalStateException(MessageFormat.format(
                    "Comment with id {0} does not belong to post with id {1}", commentId, postId));
        }

        return comment;
    }
}