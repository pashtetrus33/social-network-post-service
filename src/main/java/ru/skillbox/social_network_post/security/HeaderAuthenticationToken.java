package ru.skillbox.social_network_post.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

public class HeaderAuthenticationToken extends AbstractAuthenticationToken {

    private final List<SimpleGrantedAuthority> authorities;
    private final UUID userId;
    private final String userName;

    public HeaderAuthenticationToken(UUID userId, String userName, List<SimpleGrantedAuthority> authorities) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HeaderAuthenticationToken that = (HeaderAuthenticationToken) o;
        return Objects.equals(authorities, that.authorities) && Objects.equals(userId, that.userId) && Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), authorities, userId, userName);

    }
}