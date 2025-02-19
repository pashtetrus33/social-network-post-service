package ru.skillbox.social_network_post.controller.front_test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthTestController {


    @PostMapping("/login")
    public TokenResponse login(@RequestBody UserCredentials userCredentials) {

        log.info("Login user: {}", userCredentials.toString());
        return new TokenResponse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwidXNlcklkIjoiNmY2ZDdhOGYtMTI0My00MmNmLWI0ZGQtMjg3ZjNlZjYwZWIwIiwicm9sZXMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVVNFUiJdfQ.signature",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwidXNlcklkIjoiNmY2ZDdhOGYtMTI0My00MmNmLWI0ZGQtMjg3ZjNlZjYwZWIwIiwicm9sZXMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVVNFUiJdfQ.signature");
    }
}