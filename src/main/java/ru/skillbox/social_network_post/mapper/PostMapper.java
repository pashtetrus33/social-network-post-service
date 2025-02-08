package ru.skillbox.social_network_post.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.web.model.PagePostDto;
import ru.skillbox.social_network_post.web.model.PostDto;

@Mapper(componentModel = "spring")
public interface PostMapper {

    PostDto toPostDto(Post post);

    PagePostDto toPagePostDto(Page<Post> posts);

    Post toPost(PostDto postDto);

    void updatePostFromDto(PostDto postDto, @MappingTarget Post post);
}