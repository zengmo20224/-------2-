package com.petcare.admin.mapper;

import com.petcare.admin.entity.AdminRole;
import com.petcare.admin.entity.AdminPermission;
import com.petcare.admin.entity.AdminRolePermission;
import com.petcare.admin.mapper.AdminRoleMapper;
import com.petcare.admin.mapper.AdminPermissionMapper;
import com.petcare.admin.mapper.AdminRolePermissionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies admin_role_permission unique constraint on (role_id, permission_id).
 */
@SpringBootTest
@ActiveProfiles("test")
class AdminRolePermissionMapperTest {

    @Autowired
    private AdminRoleMapper adminRoleMapper;

    @Autowired
    private AdminPermissionMapper adminPermissionMapper;

    @Autowired
    private AdminRolePermissionMapper adminRolePermissionMapper;

    @Test
    void uniqueConstraintOnRolePermission() {
        AdminRole role = new AdminRole();
        role.setRoleCode("TEST_ROLE");
        role.setRoleName("Test Role");
        role.setStatus("ACTIVE");
        adminRoleMapper.insert(role);

        AdminPermission permission = new AdminPermission();
        permission.setPermissionCode("test:resource:action");
        permission.setPermissionName("Test Permission");
        permission.setModule("test");
        permission.setStatus("ACTIVE");
        adminPermissionMapper.insert(permission);

        AdminRolePermission rp1 = new AdminRolePermission();
        rp1.setRoleId(role.getId());
        rp1.setPermissionId(permission.getId());
        adminRolePermissionMapper.insert(rp1);

        assertNotNull(rp1.getId());

        // Duplicate (role_id, permission_id) should fail
        AdminRolePermission rp2 = new AdminRolePermission();
        rp2.setRoleId(role.getId());
        rp2.setPermissionId(permission.getId());

        assertThrows(Exception.class, () -> adminRolePermissionMapper.insert(rp2));
    }
}
