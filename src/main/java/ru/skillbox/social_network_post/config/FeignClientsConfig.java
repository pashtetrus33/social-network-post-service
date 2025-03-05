package ru.skillbox.social_network_post.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.skillbox.social_network_post.client.AccountServiceClient;
import ru.skillbox.social_network_post.client.AuthServiceClient;
import ru.skillbox.social_network_post.client.FriendServiceClient;
import ru.skillbox.social_network_post.security.SecurityUtils;

@Configuration
public class FeignClientsConfig {

    @Value("${gateway.api.url}")
    private String gatewayApiUrl;

    ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Добавляем заголовки, например, для авторизации
            requestTemplate.header("Accept", "application/json");
            String token = SecurityUtils.getToken();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }


    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    // Бин для AuthServiceClient
    @Bean
    public AuthServiceClient authServiceClient() {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new feign.slf4j.Slf4jLogger(AuthServiceClient.class))
                .logLevel(Logger.Level.FULL)
                .target(AuthServiceClient.class, gatewayApiUrl + "/api/v1/auth");
    }

    // Бин для FriendServiceClient
    @Bean
    public FriendServiceClient friendServiceClient() {
        return Feign.builder()
                .encoder(jacksonEncoder())
                .decoder(jacksonDecoder())
                .logger(new feign.slf4j.Slf4jLogger(FriendServiceClient.class))
                .logLevel(Logger.Level.FULL)
                .requestInterceptor(requestInterceptor())
                .target(FriendServiceClient.class, gatewayApiUrl + "/api/v1/friends");
    }

    // Бин для AccountServiceClient
    @Bean
    public AccountServiceClient accountServiceClient() {
        return Feign.builder()
                .encoder(jacksonEncoder())
                .decoder(jacksonDecoder())
                .logger(new feign.slf4j.Slf4jLogger(AccountServiceClient.class))
                .logLevel(Logger.Level.FULL)
                .requestInterceptor(requestInterceptor())
                .target(AccountServiceClient.class, gatewayApiUrl + "/api/v1/account");
    }

    // Бин для Jackson Encoder
    @Bean
    public Encoder jacksonEncoder() {
        return new JacksonEncoder(objectMapper);
    }

    // Бин для Jackson Decoder
    @Bean
    public Decoder jacksonDecoder() {

        return new JacksonDecoder(objectMapper);
    }
}