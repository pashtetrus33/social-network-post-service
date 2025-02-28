package ru.skillbox.social_network_post.dto;


public record LikeResponseDTO(boolean success, String reactionType, int likesCount) {

}
