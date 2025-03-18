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

    private UUID postId;  // ID поста

    private String title;  // Заголовок поста

    private Boolean isBlocked;  // Флаг блокировки

    private Boolean isDeleted;  // Флаг удаления

    private LocalDateTime publishDate;  // Дата публикации поста
}