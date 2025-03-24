package ru.skillbox.social_network_post.mapper;


import ru.skillbox.social_network_post.dto.RequestReactionDto;
import ru.skillbox.social_network_post.entity.Reaction;


public final class LikeMapperFactory {

    private LikeMapperFactory() {
        throw new UnsupportedOperationException("Utility class");
    }


    public static Reaction toReaction(RequestReactionDto requestReactionDto) {
        if (requestReactionDto == null) {
            return null;
        }

        return Reaction.builder()
                .type(requestReactionDto.type())
                .reactionType(requestReactionDto.reactionType())
                .build();
    }
}