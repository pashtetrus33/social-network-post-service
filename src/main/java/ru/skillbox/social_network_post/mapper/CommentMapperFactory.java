package ru.skillbox.social_network_post.mapper;

import java.util.List;
import java.util.UUID;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.web.model.CommentDto;
import ru.skillbox.social_network_post.web.model.PageCommentDto;

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
                comment.getIsDelete(),
                comment.getLikeAmount(),
                comment.getMyLike(),
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

        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setCommentType(commentDto.getCommentType());
        comment.setTime(commentDto.getTime());
        comment.setTimeChanged(commentDto.getTimeChanged());
        comment.setAuthorId(commentDto.getAuthorId());
        comment.setCommentText(commentDto.getCommentText());
        comment.setIsBlocked(commentDto.getIsBlocked());
        comment.setIsDelete(commentDto.getIsDelete());
        comment.setLikeAmount(commentDto.getLikeAmount());
        comment.setMyLike(commentDto.getMyLike());
        comment.setCommentsCount(commentDto.getCommentsCount());
        comment.setImagePath(commentDto.getImagePath());

        return comment;
    }

    public static void updateCommentFromDto(CommentDto commentDto, Comment comment) {
        if (commentDto == null || comment == null) {
            return;
        }

        comment.setCommentType(commentDto.getCommentType());
        comment.setTime(commentDto.getTime());
        comment.setTimeChanged(commentDto.getTimeChanged());
        comment.setAuthorId(commentDto.getAuthorId());
        comment.setCommentText(commentDto.getCommentText());
        comment.setIsBlocked(commentDto.getIsBlocked());
        comment.setIsDelete(commentDto.getIsDelete());
        comment.setLikeAmount(commentDto.getLikeAmount());
        comment.setMyLike(commentDto.getMyLike());
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