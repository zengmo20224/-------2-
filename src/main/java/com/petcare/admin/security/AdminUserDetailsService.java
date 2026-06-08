package com.petcare.admin.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.service.AdminUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads admin user details for Spring Security authentication.
 * Only allows ACTIVE and not logically-deleted admins to authenticate.
 */
@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserService adminUserService;
    private final AdminAuthorityService adminAuthorityService;

    public AdminUserDetailsService(AdminUserService adminUserService,
                                   AdminAuthorityService adminAuthorityService) {
        this.adminUserService = adminUserService;
        this.adminAuthorityService = adminAuthorityService;
    }

    /**
     * Loads admin by username. Only ACTIVE and not deleted admins are loaded.
     * The password is included here for BCrypt matching by Spring Security,
     * but will NOT be stored in the AdminPrincipal.
     *
     * @param username the admin's username
     * @return AdminPrincipal with permissions loaded from RBAC tables
     * @throws UsernameNotFoundException if admin not found, disabled, or deleted
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser admin = adminUserService.getOne(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, username)
                        .eq(AdminUser::getStatus, "ACTIVE")
        );
        if (admin == null) {
            throw new UsernameNotFoundException("管理员账号不存在或已禁用");
        }

        List<String> permissionCodes = adminAuthorityService.loadPermissionCodes(admin.getRole());

        return new AdminPrincipal(admin.getId(), admin.getUsername(), admin.getRole(), permissionCodes);
    }

    /**
     * Loads admin by ID. Used by JWT filter to reconstruct authentication from token.
     *
     * @param adminId the admin's ID
     * @return AdminPrincipal with permissions loaded from RBAC tables
     * @throws UsernameNotFoundException if admin not found, disabled, or deleted
     */
    public UserDetails loadUserByAdminId(Long adminId) throws UsernameNotFoundException {
        AdminUser admin = adminUserService.getOne(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getId, adminId)
                        .eq(AdminUser::getStatus, "ACTIVE")
        );
        if (admin == null) {
            throw new UsernameNotFoundException("管理员账号不存在或已禁用");
        }

        List<String> permissionCodes = adminAuthorityService.loadPermissionCodes(admin.getRole());

        return new AdminPrincipal(admin.getId(), admin.getUsername(), admin.getRole(), permissionCodes);
    }
}
