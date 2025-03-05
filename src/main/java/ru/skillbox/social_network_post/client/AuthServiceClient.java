package ru.skillbox.social_network_post.client;

import feign.Param;
import feign.RequestLine;

public interface AuthServiceClient {

    @RequestLine("GET /validate?token={token}")
    Boolean validateToken(@Param("token") String token);
}