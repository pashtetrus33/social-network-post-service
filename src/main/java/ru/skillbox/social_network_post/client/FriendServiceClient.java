package ru.skillbox.social_network_post.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

public interface FriendServiceClient {
    @GetMapping("/friendId")
    List<UUID> getFriendsIds(@RequestParam UUID srcPersonId);
}