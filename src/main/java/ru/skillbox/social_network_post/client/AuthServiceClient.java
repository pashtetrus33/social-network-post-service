package ru.skillbox.social_network_post.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    @GetMapping("/validate")
    Boolean validateToken(@RequestParam("token") String token);
}