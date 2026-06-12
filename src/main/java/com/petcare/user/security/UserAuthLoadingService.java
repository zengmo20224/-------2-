package com.petcare.user.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.user.entity.User;
import com.petcare.user.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user details for Spring Security authentication from USER JWT tokens.
 * Only accepts ACTIVE and not logically-deleted users.
 * Does NOT implement UserDetailsService to avoid conflict with AdminUserDetailsService.
 */
@Service
public class UserAuthLoadingService {

    private final UserService userService;

    public UserAuthLoadingService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Loads an active, non-deleted user by ID for authentication.
     *
     * @param userId the user's ID from JWT subject
     * @return UserPrincipal with userId and ROLE_USER authority
     * @throws UsernameNotFoundException if user not found, disabled, or deleted
     */
    public UserPrincipal loadActiveUserById(Long userId) {
        User user = userService.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId, userId)
                        .eq(User::getStatus, "ACTIVE")
        );
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在或已禁用");
        }
        return new UserPrincipal(user.getId());
    }
}
