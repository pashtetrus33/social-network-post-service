package ru.skillbox.social_network_post.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class ReactionDto {

    private boolean active;
    private List<ReactionInfo> reactionsInfo;
    private int quantity;
    private String reaction;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Builder
    public static class ReactionInfo {
        private String reactionType;
        private Long count;
    }
}