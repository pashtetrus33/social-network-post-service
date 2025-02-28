package ru.skillbox.social_network_post.config;

import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.skillbox.social_network_post.client.AccountServiceClient;
import ru.skillbox.social_network_post.client.AuthServiceClient;
import ru.skillbox.social_network_post.client.FriendServiceClient;
import ru.skillbox.social_network_post.security.SecurityUtils;

@Configuration
public class FreignClientsConfig {


    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Добавляем заголовки, например, для авторизации
            requestTemplate.header("Accept", "application/json");
            String token = SecurityUtils.getToken();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }


    // Уровень логирования для Feign
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;  // Полное логирование запросов
    }


    // Бин для AuthServiceClient
    @Bean
    public AuthServiceClient authServiceClient() {
        return Feign.builder()
                .encoder(jacksonEncoder())
                .decoder(jacksonDecoder())
                .logger(new feign.slf4j.Slf4jLogger(AuthServiceClient.class))
                .logLevel(Logger.Level.FULL)
                .requestInterceptor(requestInterceptor())
                .target(AuthServiceClient.class, "http://auth-service");
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
                .target(FriendServiceClient.class, "friend-service");
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
                .target(AccountServiceClient.class, "account-service");
    }

    // Бин для Jackson Encoder
    @Bean
    public Encoder jacksonEncoder() {
        return new Encoder.Default();
    }

    // Бин для Jackson Decoder
    @Bean
    public Decoder jacksonDecoder() {
        return new Decoder.Default();
    }
}