package ru.skillbox.social_network_post.mapper;


import ru.skillbox.social_network_post.dto.ReactionDto;
import ru.skillbox.social_network_post.entity.Reaction;


public final class LikeMapperFactory {

    private LikeMapperFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ReactionDto toReactionDto(Reaction reaction) {
        if (reaction == null) {
            return null;
        }

        return ReactionDto.builder()
                .reactionType(reaction.getReactionType())
                .type(reaction.getType())
                .reactionType(reaction.getReactionType())
                .build();
    }


    public static Reaction toLike(ReactionDto reactionDto) {
        if (reactionDto == null) {
            return null;
        }

        return Reaction.builder()
                .type(reactionDto.getType())
                .reactionType(reactionDto.getReactionType())
                .build();
    }
}