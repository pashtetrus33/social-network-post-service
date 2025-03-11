package ru.skillbox.social_network_post.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchDto {

    private List<UUID> ids;

    private List<UUID> accountIds;

    private List<UUID> blockedIds;

    private Boolean isBlocked;

    @Size(max = 255, message = "Author name must not exceed 255 characters")
    private String author;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String postText;

    private Boolean withFriends;

    private Boolean isDeleted;

    @Size(max = 50, message = "Maximum number of tags is 50")
    private List<String> tags;

    private String dateFrom;

    private String dateTo;
}