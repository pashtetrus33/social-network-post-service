package ru.skillbox.social_network_post.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String errorMessage;

    private String status;
}