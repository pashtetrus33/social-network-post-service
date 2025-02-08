package ru.skillbox.social_network_post.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.exception.CommentNotFoundException;
import ru.skillbox.social_network_post.exception.PostNotFoundException;
import ru.skillbox.social_network_post.mapper.CommentMapper;
import ru.skillbox.social_network_post.repository.CommentRepository;
import ru.skillbox.social_network_post.repository.PostRepository;
import ru.skillbox.social_network_post.service.CommentService;
import ru.skillbox.social_network_post.web.model.CommentDto;
import ru.skillbox.social_network_post.web.model.PageCommentDto;

import java.text.MessageFormat;


@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostRepository postRepository;


    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Override
    public PageCommentDto getByPostId(Long postId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByPostId(postId, pageable);
        return commentMapper.toPageCommentDto(comments);
    }

    @Override
    public void create(Long postId, CommentDto commentDto) {
        // Получаем комментарий из DTO
        Comment comment = commentMapper.toComment(commentDto);

        // Устанавливаем пост для комментария
        comment.setPost(postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(
                        MessageFormat.format("Post with id {0} is not found", postId))));

        // Сохраняем комментарий
        commentRepository.save(comment);
    }

    @Override
    public void update(Long postId, Long commentId, CommentDto commentDto) {
        // Ищем комментарий по id и посту
        Comment comment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> {
                    logger.error("Comment with id {} for post with id {} not found", commentId, postId);
                    return new CommentNotFoundException("Comment not found");
                });

        // Обновляем комментарий из DTO
        commentMapper.updateCommentFromDto(commentDto, comment);

        // Сохраняем обновленный комментарий
        commentRepository.save(comment);
    }

    @Override
    public void delete(Long postId, Long commentId) {
        // Проверка на существование комментария перед удалением
        if (!commentRepository.existsByIdAndPostId(commentId, postId)) {
            logger.error("Comment with id {} for post with id {} not found", commentId, postId);
            throw new CommentNotFoundException("Comment not found");
        }

        // Удаляем комментарий
        commentRepository.deleteByIdAndPostId(commentId, postId);
    }

    @Override
    public PageCommentDto getSubcomments(Long postId, Long commentId, Pageable pageable) {

        Page<Comment> subcomments = commentRepository.findByParentCommentIdAndPostId(commentId, postId, pageable);
        // Преобразуем в PageCommentDto
        return commentMapper.toCommentDtoPage(subcomments);
    }
}