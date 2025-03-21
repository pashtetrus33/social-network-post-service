package ru.skillbox.social_network_post.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import ru.skillbox.social_network_post.dto.PostDto;
import ru.skillbox.social_network_post.dto.ReactionDto;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.exception.EntityNotFoundException;
import ru.skillbox.social_network_post.exception.IdMismatchException;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;

import java.text.MessageFormat;
import java.util.UUID;

@Slf4j
public class EntityCheckUtils {

    private EntityCheckUtils() {
    }

    //Utility method to check comment presence
    public static Comment checkCommentPresence(CommentRepository commentRepository, UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Comment with ID {} not found", commentId);
                    return new EntityNotFoundException(MessageFormat.format("Comment with id {0} not found", commentId));
                });
    }

    // Utility method to check if both comment and post are present
    public static Pair<Post, Comment> checkCommentAndPostPresence(
            CommentRepository commentRepository,
            PostRepository postRepository,
            UUID postId,
            UUID commentId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Post with id {0} not found", postId)));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        MessageFormat.format("Comment with id {0} not found", commentId)));

        if (!comment.getPost().getId().equals(postId)) {
            throw new IdMismatchException(
                    MessageFormat.format("Comment with id {0} does not belong to post with id {1}", commentId, postId));
        }

        return Pair.of(post, comment);
    }


    // Utility method to check post presence and return the post if found
    public static Post checkPostPresence(PostRepository postRepository, UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(MessageFormat.format("Post with id {0} not found", postId)));
    }

    // Utility method to validate LikeDto
    public static void checkReactionDto(ReactionDto reactionDto) {
        if (reactionDto == null) {
            throw new IllegalArgumentException("Like data must not be null");
        }
    }

    // Utility method to validate PostDto
    public static void checkPostDto(PostDto postDto) {
        if (postDto == null) {
            throw new IllegalArgumentException("Post data must not be null");
        }
    }
}
