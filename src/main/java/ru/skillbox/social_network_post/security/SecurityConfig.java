package ru.skillbox.social_network_post.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // Отключаем CSRF для API
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Без сессий
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(request -> request.getHeader("X-User-Id") != null).authenticated() // Проверяем заголовок
                        .anyRequest().denyAll() // Запрещаем все остальные запросы
                )
                .addFilterBefore(new XUserIdAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class) // Добавляем наш фильтр
                .build();
    }
}