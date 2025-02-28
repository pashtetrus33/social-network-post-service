package ru.skillbox.social_network_post.dto;


import java.util.List;

public record PostReactionDTO(
        boolean success,                    // Успех операции
        List<ReactionDTO> reactions,        // Список всех реакций с количеством
        ReactionDTO myReaction              // Текущая реакция пользователя
) {

    // Вложенный record для описания каждой реакции
    public record ReactionDTO(
            String reactionType,  // Тип реакции (like, love, angry и т.д.)
            String label,         // Описание реакции (например, "Like", "Love")
            int count             // Количество реакций данного типа
    ) {}
}
