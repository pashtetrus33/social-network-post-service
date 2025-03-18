package ru.skillbox.social_network_post.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class PostNotificationDto {

    private UUID authorId;

    private UUID postId;

    private String title;

    private LocalDateTime publishDate;
}