package ru.skillbox.social_network_post.dto;

import java.util.UUID;

public record KafkaDto(UUID accountId, UUID dataId) {
}