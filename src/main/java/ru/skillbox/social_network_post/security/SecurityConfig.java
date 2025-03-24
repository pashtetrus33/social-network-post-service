package ru.skillbox.social_network_post.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.skillbox.social_network_post.client.AuthServiceClient;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthServiceClient authServiceClient;
    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/api/health", "/api/metrics", "/api/dashboards").permitAll() // Разрешить доступ ко всем путям /actuator без аутентификации
                        .anyRequest().authenticated()  // Для остальных запросов нужна аутентификация
                )
                .addFilterBefore(new TokenAuthenticationFilter(authServiceClient, jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}