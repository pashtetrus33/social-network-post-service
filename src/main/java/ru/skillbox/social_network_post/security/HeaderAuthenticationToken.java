package ru.skillbox.social_network_post.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HeaderAuthenticationToken extends AbstractAuthenticationToken {

    private final List<GrantedAuthority> authorities;
    private final UUID userId;
    private final String userName;

    public HeaderAuthenticationToken(UUID userId, String userName, List<GrantedAuthority> authorities) {
        super(authorities != null ? authorities : new ArrayList<>());
        this.userId = userId;
        this.userName = userName;
        this.authorities = authorities != null ? authorities : new ArrayList<>();
        setAuthenticated(true);
    }

    @Override
    public String getName() {
        return userName;
    }


    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public UUID getPrincipal() {
        return userId;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }
}