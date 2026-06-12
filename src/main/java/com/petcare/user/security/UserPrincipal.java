package com.petcare.user.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security principal for authenticated user (USER token).
 * Contains only userId and ROLE_USER authority.
 * Does NOT store password, phone, openid, unionid or admin permissions.
 */
public class UserPrincipal {

    private static final String ROLE_USER = "ROLE_USER";

    private final Long userId;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long userId) {
        this.userId = userId;
        this.authorities = List.of(new SimpleGrantedAuthority(ROLE_USER));
    }

    public Long getUserId() {
        return userId;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
