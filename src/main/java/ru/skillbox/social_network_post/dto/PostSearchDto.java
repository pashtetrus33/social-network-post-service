package ru.skillbox.social_network_post.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
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

    @Min(value = 0, message = "Date from must be zero or a positive number")
    private Long dateFrom;

    @Min(value = 0, message = "Date to must be zero or a positive number")
    private Long dateTo;
}