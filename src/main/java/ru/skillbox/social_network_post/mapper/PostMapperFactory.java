package ru.skillbox.social_network_post.mapper;

import org.springframework.data.domain.Page;
import ru.skillbox.social_network_post.entity.Post;
import ru.skillbox.social_network_post.dto.PagePostDto;
import ru.skillbox.social_network_post.dto.PostDto;
import ru.skillbox.social_network_post.dto.PostType;

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

        return PostDto.builder()
                .id(post.getId())
                .time(post.getTime())
                .timeChanged(post.getTimeChanged())
                .authorId(post.getAuthorId())
                .title(post.getTitle())
                .type(PostType.POSTED)
                .postText(post.getPostText())
                .isBlocked(post.getIsBlocked())
                .isDeleted(post.getIsDeleted())
                .commentsCount(post.getCommentsCount())
                .tags(post.getTags() != null ? new ArrayList<>(post.getTags()) : null)
                .likeAmount(post.getLikeAmount())
                .myLike(post.getMyLike())
                .imagePath(post.getImagePath())
                .publishDate(post.getPublishDate())
                .build();
    }

    public static PagePostDto toPagePostDto(Page<Post> posts) {
        if (posts == null) {
            return null;
        }

        return PagePostDto.builder()
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .number(posts.getNumber())
                .size(posts.getSize())
                .content(posts.hasContent() ? toPostDtoList(posts.getContent()) : List.of())
                .sort(posts.getSort())
                .first(posts.isFirst())
                .last(posts.isLast())
                .numberOfElements(posts.getNumberOfElements())
                .pageable(posts.getPageable())
                .empty(posts.isEmpty())
                .build();
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