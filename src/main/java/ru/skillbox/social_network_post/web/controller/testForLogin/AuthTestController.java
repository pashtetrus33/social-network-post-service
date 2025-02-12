package ru.skillbox.social_network_post.web.controller.testForLogin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.UserCredentialsDataSourceAdapter;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class AuthTestController {


    @PostMapping("/auth/login")
    public ResponseEntity<Void> login(@RequestBody UserCredentials userCredentials) {

        log.info("Login user: {}", userCredentials.toString());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/account/me")
    public ResponseEntity<Void> accountMe() {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}