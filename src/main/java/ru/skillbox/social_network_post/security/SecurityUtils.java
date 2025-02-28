package ru.skillbox.social_network_post.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();

    private SecurityUtils() {
    }

    // Сохранение токена в ThreadLocal (можно использовать и для других целей)
    public static void saveToken(String token) {
        tokenHolder.set(token);
    }

    // Извлечение токена из ThreadLocal
    public static String getToken() {
        return tokenHolder.get();
    }

    public static UUID getAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UUID) {
            return (UUID) authentication.getPrincipal();
        }
        throw new IllegalStateException("No authenticated user or invalid principal type");
    }
}