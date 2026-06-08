package com.petcare.admin.security;

import com.petcare.admin.entity.AdminPermission;
import com.petcare.admin.entity.AdminRole;
import com.petcare.admin.entity.AdminRolePermission;
import com.petcare.admin.service.AdminPermissionService;
import com.petcare.admin.service.AdminRolePermissionService;
import com.petcare.admin.service.AdminRoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AdminAuthorityService.
 * Uses H2 in-memory database to verify RBAC permission loading.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminAuthorityServiceTest {

    @Autowired
    private AdminAuthorityService adminAuthorityService;

    @Autowired
    private AdminRoleService adminRoleService;

    @Autowired
    private AdminPermissionService adminPermissionService;

    @Autowired
    private AdminRolePermissionService adminRolePermissionService;

    @Test
    @DisplayName("SUPER_ADMIN role should load all assigned permissions")
    void superAdminShouldLoadPermissions() {
        // Arrange: create role, permissions, and mappings
        AdminRole superAdminRole = createRole("SUPER_ADMIN", "超级管理员");
        AdminPermission perm1 = createPermission("admin:role:manage", "角色管理", "admin");
        AdminPermission perm2 = createPermission("store:config:update", "门店配置修改", "store");
        createRolePermission(superAdminRole.getId(), perm1.getId());
        createRolePermission(superAdminRole.getId(), perm2.getId());

        // Act
        List<String> permissions = adminAuthorityService.loadPermissionCodes("SUPER_ADMIN");

        // Assert
        assertThat(permissions).containsExactlyInAnyOrder("admin:role:manage", "store:config:update");
    }

    @Test
    @DisplayName("STAFF role should not have store:config:update")
    void staffShouldNotHaveStoreConfigUpdate() {
        AdminRole staffRole = createRole("STAFF", "员工");
        AdminPermission staffPerm = createPermission("booking:booking:read", "预约查看", "booking");
        AdminPermission configPerm = createPermission("store:config:update", "门店配置修改", "store");
        createRolePermission(staffRole.getId(), staffPerm.getId());
        // STAFF does NOT get store:config:update

        List<String> permissions = adminAuthorityService.loadPermissionCodes("STAFF");

        assertThat(permissions).contains("booking:booking:read");
        assertThat(permissions).doesNotContain("store:config:update");
    }

    @Test
    @DisplayName("MANAGER should not have admin:role:manage")
    void managerShouldNotHaveAdminRoleManage() {
        AdminRole managerRole = createRole("MANAGER", "经理");
        AdminPermission managerPerm = createPermission("booking:booking:confirm", "预约确认", "booking");
        AdminPermission rolePerm = createPermission("admin:role:manage", "角色管理", "admin");
        createRolePermission(managerRole.getId(), managerPerm.getId());
        // MANAGER does NOT get admin:role:manage

        List<String> permissions = adminAuthorityService.loadPermissionCodes("MANAGER");

        assertThat(permissions).contains("booking:booking:confirm");
        assertThat(permissions).doesNotContain("admin:role:manage");
    }

    @Test
    @DisplayName("Unknown role should return empty permissions")
    void unknownRoleShouldReturnEmpty() {
        List<String> permissions = adminAuthorityService.loadPermissionCodes("UNKNOWN_ROLE");
        assertThat(permissions).isEmpty();
    }

    @Test
    @DisplayName("Inactive role should return empty permissions")
    void inactiveRoleShouldReturnEmpty() {
        AdminRole inactiveRole = createRole("INACTIVE_TEST", "停用角色");
        // Set status to DISABLED
        inactiveRole.setStatus("DISABLED");
        adminRoleService.updateById(inactiveRole);

        List<String> permissions = adminAuthorityService.loadPermissionCodes("INACTIVE_TEST");
        assertThat(permissions).isEmpty();
    }

    @Test
    @DisplayName("Inactive permission should not be loaded")
    void inactivePermissionShouldNotBeLoaded() {
        AdminRole role = createRole("TEST_ROLE_ACTIVE", "测试角色");
        AdminPermission activePerm = createPermission("test:active:perm", "活跃权限", "test");
        AdminPermission inactivePerm = createPermission("test:inactive:perm", "停用权限", "test");
        inactivePerm.setStatus("DISABLED");
        adminPermissionService.updateById(inactivePerm);

        createRolePermission(role.getId(), activePerm.getId());
        createRolePermission(role.getId(), inactivePerm.getId());

        List<String> permissions = adminAuthorityService.loadPermissionCodes("TEST_ROLE_ACTIVE");

        assertThat(permissions).containsExactly("test:active:perm");
        assertThat(permissions).doesNotContain("test:inactive:perm");
    }

    // --- helper methods ---

    private AdminRole createRole(String roleCode, String roleName) {
        AdminRole role = new AdminRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setStatus("ACTIVE");
        adminRoleService.save(role);
        return role;
    }

    private AdminPermission createPermission(String code, String name, String module) {
        AdminPermission perm = new AdminPermission();
        perm.setPermissionCode(code);
        perm.setPermissionName(name);
        perm.setModule(module);
        perm.setStatus("ACTIVE");
        adminPermissionService.save(perm);
        return perm;
    }

    private void createRolePermission(Long roleId, Long permissionId) {
        AdminRolePermission rp = new AdminRolePermission();
        rp.setRoleId(roleId);
        rp.setPermissionId(permissionId);
        adminRolePermissionService.save(rp);
    }
}
