package com.petcare.admin.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security UserDetails implementation for admin users.
 * Contains adminId, username, role, and permission-based authorities.
 * Does NOT contain the password.
 */
public class AdminPrincipal implements UserDetails {

    private final Long adminId;
    private final String username;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public AdminPrincipal(Long adminId, String username, String role,
                          Collection<String> permissionCodes) {
        this.adminId = adminId;
        this.username = username;
        this.role = role;
        this.authorities = permissionCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .map(a -> (GrantedAuthority) a)
                .toList();
    }

    public Long getAdminId() {
        return adminId;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // Password is never stored in the principal
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Returns the list of permission code strings.
     */
    public List<String> getPermissionCodes() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}
