package ru.skillbox.social_network_post.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.web.model.CommentDto;
import ru.skillbox.social_network_post.web.model.PageCommentDto;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentDto toCommentDto(Comment comment);

    PageCommentDto toPageCommentDto(Page<Comment> comments);

    Comment toComment(CommentDto commentDto);

    // Используем аннотацию @MappingTarget для обновления объекта
    void updateCommentFromDto(CommentDto commentDto, @MappingTarget Comment comment);

    PageCommentDto toCommentDtoPage(Page<Comment> comments);
}