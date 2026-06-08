package com.petcare.service.controller;

import com.petcare.admin.entity.AdminPermission;
import com.petcare.admin.entity.AdminRole;
import com.petcare.admin.entity.AdminRolePermission;
import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.service.AdminPermissionService;
import com.petcare.admin.service.AdminRolePermissionService;
import com.petcare.admin.service.AdminRoleService;
import com.petcare.admin.service.AdminUserService;
import com.petcare.common.security.JwtTokenService;
import com.petcare.service.entity.ServiceCategory;
import com.petcare.service.entity.ServiceItem;
import com.petcare.service.service.ServiceCategoryService;
import com.petcare.service.service.ServiceItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for ServiceCatalogController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ServiceCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServiceCategoryService serviceCategoryService;

    @Autowired
    private ServiceItemService serviceItemService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AdminRoleService adminRoleService;

    @Autowired
    private AdminPermissionService adminPermissionService;

    @Autowired
    private AdminRolePermissionService adminRolePermissionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    private String authToken;

    @BeforeEach
    void setUp() {
        Long adminId = createTestAdmin("catalogtest", "password123456", "SUPER_ADMIN");
        setupRoleAndPermission("SUPER_ADMIN", "booking:booking:read", "booking");
        authToken = jwtTokenService.signAdminToken(adminId, "catalogtest", "SUPER_ADMIN");
    }

    @Test
    @DisplayName("GET /service-categories returns active categories")
    void listCategoriesReturnsActive() throws Exception {
        createCategory("美容", 1);
        createCategory("洗澡", 2);

        mockMvc.perform(get("/api/v1/service-categories")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("美容"));
    }

    @Test
    @DisplayName("GET /service-items returns paginated ON_SALE items")
    void listItemsReturnsOnSale() throws Exception {
        ServiceCategory cat = createCategory("美容", 1);
        createItem(cat.getId(), "洗澡服务", "STORE", new BigDecimal("99.00"), 60, "ON_SALE");
        createItem(cat.getId(), "下架服务", "STORE", new BigDecimal("50.00"), 30, "OFF_SALE");

        mockMvc.perform(get("/api/v1/service-items")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("洗澡服务"));
    }

    @Test
    @DisplayName("GET /service-items filters by categoryId")
    void filterByCategoryId() throws Exception {
        ServiceCategory cat1 = createCategory("美容", 1);
        ServiceCategory cat2 = createCategory("遛狗", 2);
        createItem(cat1.getId(), "洗澡", "STORE", new BigDecimal("99.00"), 60, "ON_SALE");
        createItem(cat2.getId(), "遛狗上门", "HOME", new BigDecimal("80.00"), 60, "ON_SALE");

        mockMvc.perform(get("/api/v1/service-items")
                        .param("categoryId", cat1.getId().toString())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("洗澡"));
    }

    @Test
    @DisplayName("GET /service-items filters by serviceMode")
    void filterByServiceMode() throws Exception {
        ServiceCategory cat = createCategory("服务", 1);
        createItem(cat.getId(), "到店服务", "STORE", new BigDecimal("99.00"), 60, "ON_SALE");
        createItem(cat.getId(), "上门服务", "HOME", new BigDecimal("120.00"), 90, "ON_SALE");
        createItem(cat.getId(), "通用服务", "BOTH", new BigDecimal("100.00"), 60, "ON_SALE");

        mockMvc.perform(get("/api/v1/service-items")
                        .param("serviceMode", "HOME")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(2)); // HOME + BOTH
    }

    @Test
    @DisplayName("GET /service-items/{id} returns single item detail")
    void getItemReturnsDetail() throws Exception {
        ServiceCategory cat = createCategory("美容", 1);
        ServiceItem item = createItem(cat.getId(), "精洗服务", "STORE",
                new BigDecimal("128.00"), 90, "ON_SALE");

        mockMvc.perform(get("/api/v1/service-items/" + item.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("精洗服务"))
                .andExpect(jsonPath("$.data.price").value(128.00))
                .andExpect(jsonPath("$.data.durationMinutes").value(90));
    }

    @Test
    @DisplayName("GET /service-items/{id} with non-existent ID returns 404")
    void getItemNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/service-items/999999999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("resource_not_found"));
    }

    @Test
    @DisplayName("GET /service-items/{id} for OFF_SALE item returns 404")
    void offSaleItemReturns404() throws Exception {
        ServiceCategory cat = createCategory("美容", 1);
        ServiceItem item = createItem(cat.getId(), "下架", "STORE",
                new BigDecimal("50.00"), 30, "OFF_SALE");

        mockMvc.perform(get("/api/v1/service-items/" + item.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Unauthenticated request to service-items returns 401")
    void unauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/service-items"))
                .andExpect(status().isUnauthorized());
    }

    // --- helper methods ---

    private ServiceCategory createCategory(String name, int sort) {
        ServiceCategory cat = new ServiceCategory();
        cat.setName(name);
        cat.setSort(sort);
        cat.setStatus("ACTIVE");
        serviceCategoryService.save(cat);
        return cat;
    }

    private ServiceItem createItem(Long categoryId, String name, String serviceMode,
                                  BigDecimal price, int duration, String status) {
        ServiceItem item = new ServiceItem();
        item.setCategoryId(categoryId);
        item.setName(name);
        item.setServiceMode(serviceMode);
        item.setPrice(price);
        item.setDurationMinutes(duration);
        item.setPetType("ALL");
        item.setPetSize("ALL");
        item.setNeedAddress("HOME".equals(serviceMode) ? 1 : 0);
        item.setNeedPet(1);
        item.setStatus(status);
        item.setSort(0);
        serviceItemService.save(item);
        return item;
    }

    private Long createTestAdmin(String username, String rawPassword, String role) {
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRole(role);
        admin.setStatus("ACTIVE");
        adminUserService.save(admin);
        return admin.getId();
    }

    private void setupRoleAndPermission(String roleCode, String permCode, String module) {
        AdminRole role = new AdminRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);
        role.setStatus("ACTIVE");
        adminRoleService.save(role);

        AdminPermission perm = new AdminPermission();
        perm.setPermissionCode(permCode);
        perm.setPermissionName(permCode);
        perm.setModule(module);
        perm.setStatus("ACTIVE");
        adminPermissionService.save(perm);

        AdminRolePermission rp = new AdminRolePermission();
        rp.setRoleId(role.getId());
        rp.setPermissionId(perm.getId());
        adminRolePermissionService.save(rp);
    }
}
