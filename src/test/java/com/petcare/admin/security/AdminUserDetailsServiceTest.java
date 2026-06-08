package com.petcare.admin.security;

import com.petcare.admin.entity.AdminPermission;
import com.petcare.admin.entity.AdminRole;
import com.petcare.admin.entity.AdminRolePermission;
import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.service.AdminPermissionService;
import com.petcare.admin.service.AdminRolePermissionService;
import com.petcare.admin.service.AdminRoleService;
import com.petcare.admin.service.AdminUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for AdminUserDetailsService.
 * Verifies admin loading and permission code injection.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminUserDetailsServiceTest {

    @Autowired
    private AdminUserDetailsService adminUserDetailsService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AdminRoleService adminRoleService;

    @Autowired
    private AdminPermissionService adminPermissionService;

    @Autowired
    private AdminRolePermissionService adminRolePermissionService;

    @Test
    @DisplayName("Should load admin by username with permissions")
    void shouldLoadAdminByUsernameWithPermissions() {
        setupAdminWithRole("superadmin", "SUPER_ADMIN", "admin:role:manage");

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername("superadmin");

        assertThat(userDetails).isInstanceOf(AdminPrincipal.class);
        AdminPrincipal principal = (AdminPrincipal) userDetails;
        assertThat(principal.getUsername()).isEqualTo("superadmin");
        assertThat(principal.getRole()).isEqualTo("SUPER_ADMIN");
        assertThat(principal.getPassword()).isNull();
        assertThat(principal.getPermissionCodes()).contains("admin:role:manage");
    }

    @Test
    @DisplayName("Should load admin by ID with permissions")
    void shouldLoadAdminByIdWithPermissions() {
        Long adminId = setupAdminWithRole("manager1", "MANAGER", "booking:booking:confirm");

        UserDetails userDetails = adminUserDetailsService.loadUserByAdminId(adminId);

        assertThat(userDetails).isInstanceOf(AdminPrincipal.class);
        AdminPrincipal principal = (AdminPrincipal) userDetails;
        assertThat(principal.getAdminId()).isEqualTo(adminId);
        assertThat(principal.getRole()).isEqualTo("MANAGER");
        assertThat(principal.getPermissionCodes()).contains("booking:booking:confirm");
    }

    @Test
    @DisplayName("Should throw for non-existent username")
    void shouldThrowForNonExistentUsername() {
        assertThatThrownBy(() -> adminUserDetailsService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw for non-existent admin ID")
    void shouldThrowForNonExistentAdminId() {
        assertThatThrownBy(() -> adminUserDetailsService.loadUserByAdminId(999999L))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw for disabled admin")
    void shouldThrowForDisabledAdmin() {
        AdminUser admin = createAdmin("disabled_admin", "STAFF");
        admin.setStatus("DISABLED");
        adminUserService.updateById(admin);

        assertThatThrownBy(() -> adminUserDetailsService.loadUserByUsername("disabled_admin"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("Authorities should contain permission codes")
    void authoritiesShouldContainPermissionCodes() {
        setupAdminWithRole("staff_perm_test", "STAFF", "booking:booking:read");

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername("staff_perm_test");

        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("booking:booking:read");
    }

    // --- helper methods ---

    private Long setupAdminWithRole(String username, String roleCode, String... permissionCodes) {
        AdminUser admin = createAdmin(username, roleCode);

        AdminRole role = new AdminRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);
        role.setStatus("ACTIVE");
        adminRoleService.save(role);

        for (String code : permissionCodes) {
            AdminPermission perm = new AdminPermission();
            perm.setPermissionCode(code);
            perm.setPermissionName(code);
            perm.setModule(code.split(":")[0]);
            perm.setStatus("ACTIVE");
            adminPermissionService.save(perm);

            AdminRolePermission rp = new AdminRolePermission();
            rp.setRoleId(role.getId());
            rp.setPermissionId(perm.getId());
            adminRolePermissionService.save(rp);
        }

        return admin.getId();
    }

    private AdminUser createAdmin(String username, String role) {
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        // BCrypt hash for "password123" - not a real password concern in test
        admin.setPassword("$2a$12$LJ3m4ys3NzBJSdVg8VPVMuHCFsDGZbsSSTjGBniQwWqFQTITaFmyW");
        admin.setRole(role);
        admin.setStatus("ACTIVE");
        adminUserService.save(admin);
        return admin;
    }
}
