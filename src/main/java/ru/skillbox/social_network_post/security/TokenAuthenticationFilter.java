package ru.skillbox.social_network_post.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.skillbox.social_network_post.client.AuthServiceClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthServiceClient authServiceClient; // Сервис для валидации токена через Feign
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        String path = request.getServletPath();
        log.info("Request path: {}", path);

        // Пропустить проверку токена для маршрутов /auth/** и /account/**
        if (path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/account/me")) {
            log.info("Request path: {}", path);

            Authentication authentication = new HeaderAuthenticationToken(UUID.fromString("41d948a2-af59-4f88-a7b6-283d009934a0"),
                    "test", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null) {
            unauthorizedResponse(response, "{\"error\": \"Missing Authorization header\"}");
            return;
        }
        if (!authHeader.startsWith("Bearer ")) {
            unauthorizedResponse(response, "{\"error\": \"Invalid token format\"}");
            return;
        }

        String token = authHeader.substring(7);

        //boolean isTokenValid = authServiceClient.validateToken(token);
        boolean isTokenValid = true;
        if (!isTokenValid) {
            unauthorizedResponse(response, "{\"error\": \"Invalid or expired token\"}");
            return;
        }

        String userName;
        UUID userId;

        try {
            userName = jwtUtil.extractUsername(token);
            userId = jwtUtil.extractUserId(token);
        } catch (IllegalArgumentException e) {
            unauthorizedResponse(response, e.getMessage());
            return;
        }


        List<String> roles = jwtUtil.extractRoles(token);


        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Authentication authentication = new HeaderAuthenticationToken(userId, userName, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void unauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
    }
}