package ru.skillbox.social_network_post.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.service.LikeService;
import ru.skillbox.social_network_post.exception.CommentNotFoundException;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;


    @Override
    public void addLikeToPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getMyLike()) {
            post.setLikeAmount(post.getLikeAmount() + 1);
            post.setMyLike(true);
            postRepository.save(post);
        }
    }

    @Override
    public void removeLikeFromPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getMyLike()) {
            post.setLikeAmount(post.getLikeAmount() - 1);
            post.setMyLike(false);
            postRepository.save(post);
        }
    }

    @Override
    public void addLikeToComment(Long postId, Long commentId) {
        // Находим комментарий по ID
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        // Проверяем, поставил ли пользователь лайк
        if (!comment.getMyLike()) {
            // Увеличиваем количество лайков для комментария
            comment.setLikeAmount(comment.getLikeAmount() + 1);
            comment.setMyLike(true);
            commentRepository.save(comment);
        }
    }

    @Override
    public void removeLikeFromComment(Long postId, Long commentId) {
        // Находим комментарий по ID
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));

        // Проверяем, убрал ли пользователь лайк
        if (comment.getMyLike()) {
            // Уменьшаем количество лайков для комментария
            comment.setLikeAmount(comment.getLikeAmount() - 1);
            comment.setMyLike(false);
            commentRepository.save(comment);
        }
    }
}