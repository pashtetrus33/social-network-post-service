package ru.skillbox.social_network_post.client;

import feign.Param;
import feign.RequestLine;

import java.util.List;
import java.util.UUID;

public interface FriendServiceClient {

    @RequestLine("GET /{friendId}")
    List<UUID> getFriendsIds(@Param("friendId") UUID friendId);
}