package ru.skillbox.social_network_post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletedAccountEventDto {

    private UUID accountId;

    private UUID userId;
}