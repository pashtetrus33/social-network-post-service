package ru.skillbox.social_network_post.mapper;

import java.util.List;
import java.util.UUID;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.dto.CommentDto;
import ru.skillbox.social_network_post.dto.PageCommentDto;

@UtilityClass
public final class CommentMapperFactory {

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return new CommentDto(
                comment.getId(),
                comment.getCommentType(),
                comment.getTime(),
                comment.getTimeChanged(),
                comment.getAuthorId(),
                getParentCommentId(comment),
                comment.getCommentText(),
                getPostId(comment),
                comment.getIsBlocked(),
                comment.getIsDeleted(),
                comment.getLikeAmount(),
                false,
                comment.getCommentsCount(),
                comment.getImagePath()
        );
    }

    public static PageCommentDto toPageCommentDto(Page<Comment> comments) {
        if (comments == null) {
            return null;
        }

        return new PageCommentDto(
                comments.getTotalElements(),
                comments.getTotalPages(),
                comments.getNumber(),
                comments.getSize(),
                comments.hasContent() ? toCommentDtoList(comments.getContent()) : List.of(),
                comments.getSort(),
                comments.isFirst(),
                comments.isLast(),
                comments.getNumberOfElements(),
                comments.getPageable(),
                comments.isEmpty()
        );
    }

    public static Comment toComment(CommentDto commentDto) {
        if (commentDto == null) {
            return null;
        }

        if (commentDto.getCommentsCount() == null) {
            commentDto.setCommentsCount(0);
        }

        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        createComment(commentDto, comment);

        return comment;
    }


    public static void updateCommentFromDto(CommentDto commentDto, Comment comment) {
        if (commentDto == null || comment == null) {
            return;
        }

        createComment(commentDto, comment);
    }


    private static void createComment(CommentDto commentDto, Comment comment) {
        comment.setCommentType(commentDto.getCommentType());
        comment.setTime(commentDto.getTime());
        comment.setTimeChanged(commentDto.getTimeChanged());
        comment.setAuthorId(commentDto.getAuthorId());
        comment.setCommentText(commentDto.getCommentText());
        comment.setIsBlocked(commentDto.getIsBlocked());
        comment.setIsDeleted(commentDto.getIsDeleted());
        comment.setLikeAmount(commentDto.getLikeAmount());
        comment.setCommentsCount(commentDto.getCommentsCount());
        comment.setImagePath(commentDto.getImagePath());
    }

    public static List<CommentDto> toCommentDtoList(List<Comment> comments) {
        return (comments == null) ? List.of() : comments.stream().map(CommentMapperFactory::toCommentDto).toList();
    }

    private static UUID getParentCommentId(Comment comment) {
        return comment != null && comment.getParentComment() != null ? comment.getParentComment().getId() : null;
    }

    private static UUID getPostId(Comment comment) {
        return comment != null && comment.getPost() != null ? comment.getPost().getId() : null;
    }
}