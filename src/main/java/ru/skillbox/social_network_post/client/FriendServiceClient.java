package ru.skillbox.social_network_post.client;

import feign.RequestLine;

import java.util.List;
import java.util.UUID;

public interface FriendServiceClient {

    @RequestLine("GET /friendId")
    List<UUID> getFriendsIds();
}