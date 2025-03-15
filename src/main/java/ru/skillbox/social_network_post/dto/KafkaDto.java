package ru.skillbox.social_network_post.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record KafkaDto(UUID accountId, UUID dataId) {
}