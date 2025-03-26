package ru.skillbox.social_network_post.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
}