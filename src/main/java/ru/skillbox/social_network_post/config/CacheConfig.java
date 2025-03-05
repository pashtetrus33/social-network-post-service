package ru.skillbox.social_network_post.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "posts",        // Кэш постов по ID
                "post_pages",    // Кэш страниц с постами
                "comments"     // Кэш комментариев

        ) {
            @Override
            protected org.springframework.cache.concurrent.ConcurrentMapCache createConcurrentMapCache(String name) {
                return new org.springframework.cache.concurrent.ConcurrentMapCache(
                        name,
                        new ConcurrentHashMap<>(500), // Ограничение в 500 записей
                        false // Без сериализации
                );
            }
        };
    }
}