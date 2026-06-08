package com.petcare.admin.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.admin.entity.AdminPermission;
import com.petcare.admin.entity.AdminRole;
import com.petcare.admin.entity.AdminRolePermission;
import com.petcare.admin.service.AdminPermissionService;
import com.petcare.admin.service.AdminRolePermissionService;
import com.petcare.admin.service.AdminRoleService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service that loads permission codes for an admin based on their role.
 *
 * Loading logic:
 * 1. Find admin_role by role code (only ACTIVE and not deleted)
 * 2. Find role_permission mappings for that role
 * 3. Find permissions (only ACTIVE and not deleted)
 * 4. Return permission_code strings as Spring Security authorities
 */
@Service
public class AdminAuthorityService {

    private final AdminRoleService adminRoleService;
    private final AdminRolePermissionService adminRolePermissionService;
    private final AdminPermissionService adminPermissionService;

    public AdminAuthorityService(AdminRoleService adminRoleService,
                                 AdminRolePermissionService adminRolePermissionService,
                                 AdminPermissionService adminPermissionService) {
        this.adminRoleService = adminRoleService;
        this.adminRolePermissionService = adminRolePermissionService;
        this.adminPermissionService = adminPermissionService;
    }

    /**
     * Loads permission codes for the given role code.
     *
     * @param roleCode the admin's role code (e.g. SUPER_ADMIN, MANAGER, STAFF)
     * @return list of permission code strings, or empty list if role not found/inactive
     */
    public List<String> loadPermissionCodes(String roleCode) {
        // 1. Find the role by code, only ACTIVE and not deleted
        AdminRole role = adminRoleService.getOne(
                new LambdaQueryWrapper<AdminRole>()
                        .eq(AdminRole::getRoleCode, roleCode)
                        .eq(AdminRole::getStatus, "ACTIVE")
        );
        if (role == null) {
            return Collections.emptyList();
        }

        // 2. Get role-permission mappings
        List<AdminRolePermission> rolePermissions = adminRolePermissionService.list(
                new LambdaQueryWrapper<AdminRolePermission>()
                        .eq(AdminRolePermission::getRoleId, role.getId())
        );
        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Extract permission IDs
        List<Long> permissionIds = rolePermissions.stream()
                .map(AdminRolePermission::getPermissionId)
                .toList();

        // 4. Load ACTIVE, not deleted permissions
        List<AdminPermission> permissions = adminPermissionService.list(
                new LambdaQueryWrapper<AdminPermission>()
                        .in(AdminPermission::getId, permissionIds)
                        .eq(AdminPermission::getStatus, "ACTIVE")
        );

        // 5. Return permission codes
        return permissions.stream()
                .map(AdminPermission::getPermissionCode)
                .toList();
    }
}
