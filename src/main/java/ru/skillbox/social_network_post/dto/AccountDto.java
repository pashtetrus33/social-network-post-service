package ru.skillbox.social_network_post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {

    private UUID id;

    private String email;

    private String phone;

    private String photo;

    private String about;

    private String city;

    private String country;

    private String token;

    private Status statusCode;

    private String firstName;

    private String lastName;

    private LocalDateTime regDate;

    private LocalDateTime birthDate;

    private String messagePermission;

    private LocalDateTime lastOnlineTime;

    private boolean isOnline;

    private boolean isBlocked;

    private boolean isDeleted;

    private String photoId;

    private String photoName;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    private String password;
}
