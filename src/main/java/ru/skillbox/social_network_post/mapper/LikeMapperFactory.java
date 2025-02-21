package ru.skillbox.social_network_post.mapper;


import ru.skillbox.social_network_post.dto.LikeDto;
import ru.skillbox.social_network_post.entity.Like;


public final class LikeMapperFactory {

    private LikeMapperFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static LikeDto toLikeDto(Like like) {
        if (like == null) {
            return null;
        }

        return new LikeDto(
                like.getType(),
                like.getReactionType()
        );
    }


    public static Like toLike(LikeDto likeDto) {
        if (likeDto == null) {
            return null;
        }

        return Like.builder()
                .type(likeDto.type())
                .reactionType(likeDto.reactionType())
                .build();
    }
}