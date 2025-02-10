package ru.skillbox.social_network_post.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import ru.skillbox.social_network_post.entity.Comment;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.web.model.CommentDto;
import ru.skillbox.social_network_post.web.model.PageCommentDto;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "parentComment.id", target = "parentId")
    @Mapping(source = "post.id", target = "postId")
    CommentDto toCommentDto(Comment comment);

    PageCommentDto toPageCommentDto(Page<Comment> comments);

    @Mapping(target = "parentComment", expression = "java(createCommentFromId(commentDto.getParentId()))")
    @Mapping(target = "post", expression = "java(createPostFromId(commentDto.getPostId()))")
    Comment toComment(CommentDto commentDto);

    @Mapping(target = "parentComment", expression = "java(createCommentFromId(commentDto.getParentId()))")
    @Mapping(target = "post", expression = "java(createPostFromId(commentDto.getPostId()))")
    void updateCommentFromDto(CommentDto commentDto, @MappingTarget Comment comment);


    default Comment createCommentFromId(Long parentId) {
        if (parentId == null) return null;
        Comment comment = new Comment();
        comment.setId(parentId);
        return comment;
    }

    default Post createPostFromId(Long postId) {
        if (postId == null) return null;
        Post post = new Post();
        post.setId(postId);
        return post;
    }
}