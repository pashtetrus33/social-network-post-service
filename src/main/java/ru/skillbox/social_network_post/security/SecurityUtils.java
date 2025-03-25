package ru.skillbox.social_network_post.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@Slf4j
public class SecurityUtils {

    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();

    private SecurityUtils() {
    }

    // Сохранение токена в ThreadLocal
    public static void saveToken(String token) {
        tokenHolder.set(token);
    }

    // Удаление токена из ThreadLocal
    public static void removeToken() {
        tokenHolder.remove();
    }

    // Извлечение токена из ThreadLocal
    public static String getToken() {
        return tokenHolder.get();
    }

    public static UUID getAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UUID accountId) {
            log.warn("Get account method. Account is: {}", accountId);
            return accountId;
        }
        throw new IllegalStateException("No authenticated user or invalid principal type");
    }
}