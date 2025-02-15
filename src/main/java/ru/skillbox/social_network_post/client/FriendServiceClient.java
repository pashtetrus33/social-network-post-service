package ru.skillbox.social_network_post.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "friend-service")
public interface FriendServiceClient {
    @GetMapping("/friendId")
    List<UUID> getFriendsIds(@RequestParam UUID srcPersonId);
}