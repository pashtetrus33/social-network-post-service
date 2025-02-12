package ru.skillbox.social_network_post.mapper;

import org.springframework.data.domain.Page;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.web.model.PagePostDto;
import ru.skillbox.social_network_post.web.model.PostDto;

import java.util.ArrayList;
import java.util.List;

public final class PostMapperFactory {

    private PostMapperFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static PostDto toPostDto(Post post) {
        if (post == null) {
            return null;
        }

        return new PostDto(
                post.getId(),
                post.getTime(),
                post.getTimeChanged(),
                post.getAuthorId(),
                post.getTitle(),
                post.getType(),
                post.getPostText(),
                post.getIsBlocked(),
                post.getIsDeleted(),
                post.getCommentsCount(),
                post.getTags() != null ? new ArrayList<>(post.getTags()) : null,
                post.getLikeAmount(),
                post.getMyLike(),
                post.getImagePath(),
                post.getPublishDate()
        );
    }

    public static PagePostDto toPagePostDto(Page<Post> posts) {
        if (posts == null) {
            return null;
        }

        return new PagePostDto(
                posts.getTotalElements(),
                posts.getTotalPages(),
                posts.getNumber(),
                posts.getSize(),
                posts.hasContent() ? toPostDtoList(posts.getContent()) : List.of(),
                posts.getSort(),
                posts.isFirst(),
                posts.isLast(),
                posts.getNumberOfElements(),
                posts.getPageable(),
                posts.isEmpty()
        );
    }

    public static Post toPost(PostDto postDto) {
        if (postDto == null) {
            return null;
        }

        return Post.builder()
                .time(postDto.getTime())
                .timeChanged(postDto.getTimeChanged())
                .authorId(postDto.getAuthorId())
                .title(postDto.getTitle())
                .type(postDto.getType())
                .postText(postDto.getPostText())
                .isBlocked(postDto.getIsBlocked())
                .isDeleted(postDto.getIsDeleted())
                .commentsCount(postDto.getCommentsCount())
                .tags(postDto.getTags() != null ? new ArrayList<>(postDto.getTags()) : null)
                .likeAmount(postDto.getLikeAmount())
                .myLike(postDto.getMyLike())
                .imagePath(postDto.getImagePath())
                .publishDate(postDto.getPublishDate())
                .build();
    }

    public static void updatePostFromDto(PostDto postDto, Post post) {
        if (postDto == null || post == null) {
            return;
        }

        post.setTime(postDto.getTime());
        post.setTimeChanged(postDto.getTimeChanged());
        post.setAuthorId(postDto.getAuthorId());
        post.setTitle(postDto.getTitle());
        post.setType(postDto.getType());
        post.setPostText(postDto.getPostText());
        post.setIsBlocked(postDto.getIsBlocked());
        post.setIsDeleted(postDto.getIsDeleted());
        post.setCommentsCount(postDto.getCommentsCount());

        if (postDto.getTags() != null) {
            post.setTags(new ArrayList<>(postDto.getTags()));
        } else {
            post.setTags(null);
        }

        post.setLikeAmount(postDto.getLikeAmount());
        post.setMyLike(postDto.getMyLike());
        post.setImagePath(postDto.getImagePath());
        post.setPublishDate(postDto.getPublishDate());
    }

    public static List<PostDto> toPostDtoList(List<Post> posts) {
        return (posts == null) ? List.of() : posts.stream().map(PostMapperFactory::toPostDto).toList();
    }
}