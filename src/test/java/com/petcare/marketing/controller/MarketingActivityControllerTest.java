package com.petcare.marketing.controller;

import com.petcare.admin.entity.AdminPermission;
import com.petcare.admin.entity.AdminRole;
import com.petcare.admin.entity.AdminRolePermission;
import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.service.AdminPermissionService;
import com.petcare.admin.service.AdminRolePermissionService;
import com.petcare.admin.service.AdminRoleService;
import com.petcare.admin.service.AdminUserService;
import com.petcare.common.security.JwtTokenService;
import com.petcare.marketing.entity.ActivityProduct;
import com.petcare.marketing.entity.ActivityService;
import com.petcare.marketing.entity.MarketingActivity;
import com.petcare.marketing.service.ActivityProductService;
import com.petcare.marketing.service.ActivityServiceService;
import com.petcare.marketing.service.MarketingActivityService;
import com.petcare.product.entity.Product;
import com.petcare.product.entity.ProductCategory;
import com.petcare.product.service.ProductCategoryService;
import com.petcare.product.service.ProductService;
import com.petcare.service.entity.ServiceCategory;
import com.petcare.service.entity.ServiceItem;
import com.petcare.service.service.ServiceCategoryService;
import com.petcare.service.service.ServiceItemService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MarketingActivityControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AdminUserService adminUserService;
    @Autowired private AdminRoleService adminRoleService;
    @Autowired private AdminPermissionService adminPermissionService;
    @Autowired private AdminRolePermissionService adminRolePermissionService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenService jwtTokenService;
    @Autowired private ProductCategoryService productCategoryService;
    @Autowired private ProductService productService;
    @Autowired private ServiceCategoryService serviceCategoryService;
    @Autowired private ServiceItemService serviceItemService;
    @Autowired private MarketingActivityService marketingActivityService;
    @Autowired private ActivityProductService activityProductService;
    @Autowired private ActivityServiceService activityServiceService;

    private String managerToken;
    private String readOnlyToken;
    private String noPermissionToken;
    private Long productId;
    private Long serviceItemId;

    @BeforeEach
    void setUp() {
        managerToken = createAdmin("activity_manager", "ACTIVITY_MANAGER",
                "marketing:activity:read", "marketing:activity:manage");
        readOnlyToken = createAdmin("activity_reader", "ACTIVITY_READER", "marketing:activity:read");
        noPermissionToken = createAdmin("activity_viewer", "ACTIVITY_VIEWER", "booking:booking:read");

        ProductCategory productCategory = new ProductCategory();
        productCategory.setName("活动商品分类");
        productCategory.setStatus("ACTIVE");
        productCategory.setSort(1);
        productCategoryService.save(productCategory);

        Product product = new Product();
        product.setCategoryId(productCategory.getId());
        product.setName("活动零食");
        product.setCoverUrl("/uploads/images/activity-product.jpg");
        product.setPrice(new BigDecimal("35.00"));
        product.setStock(20);
        product.setSalesCount(8);
        product.setPickupOnly(1);
        product.setStatus("ON_SALE");
        product.setSort(1);
        productService.save(product);
        productId = product.getId();

        ServiceCategory serviceCategory = new ServiceCategory();
        serviceCategory.setName("活动服务分类");
        serviceCategory.setStatus("ACTIVE");
        serviceCategory.setSort(1);
        serviceCategoryService.save(serviceCategory);

        ServiceItem serviceItem = new ServiceItem();
        serviceItem.setCategoryId(serviceCategory.getId());
        serviceItem.setName("活动洗护");
        serviceItem.setServiceMode("STORE");
        serviceItem.setPrice(new BigDecimal("99.00"));
        serviceItem.setDurationMinutes(60);
        serviceItem.setPetType("ALL");
        serviceItem.setPetSize("ALL");
        serviceItem.setNeedAddress(0);
        serviceItem.setNeedPet(1);
        serviceItem.setCoverUrl("/uploads/images/activity-service.jpg");
        serviceItem.setStatus("ON_SALE");
        serviceItem.setSort(1);
        serviceItemService.save(serviceItem);
        serviceItemId = serviceItem.getId();
    }

    @Test
    void adminActivityCrudUsesDedicatedMarketingPermissionsAndCoverImage() throws Exception {
        mockMvc.perform(get("/api/v1/admin/activities"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/activities")
                        .header("Authorization", bearer(noPermissionToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/activities")
                        .header("Authorization", bearer(managerToken)))
                .andExpect(status().isOk());

        String payload = """
                {"title":"周末到店活动","activityType":"MIXED","description":"服务商品一起参加",
                 "coverUrl":"/uploads/images/weekend.jpg",
                 "startTime":"2026-06-01T09:00:00","endTime":"2026-07-01T21:00:00",
                 "productIds":[%d],"serviceItemIds":[%d]}
                """.formatted(productId, serviceItemId);

        String location = mockMvc.perform(post("/api/v1/admin/activities")
                        .header("Authorization", bearer(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("周末到店活动"))
                .andExpect(jsonPath("$.data.coverUrl").value("/uploads/images/weekend.jpg"))
                .andExpect(jsonPath("$.data.productIds[0]").value(productId.toString()))
                .andExpect(jsonPath("$.data.serviceItemIds[0]").value(serviceItemId.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createdId = location.replaceAll("(?s).*\"id\":\"([0-9]+)\".*", "$1");

        mockMvc.perform(post("/api/v1/admin/activities/" + createdId + "/status")
                        .header("Authorization", bearer(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/admin/activities/" + createdId)
                        .header("Authorization", bearer(readOnlyToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicActivitiesOnlyExposeCurrentActiveAndClickableAssociations() throws Exception {
        MarketingActivity active = createActivity("当前活动", "ACTIVE",
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7),
                "/uploads/images/current.jpg");
        linkProduct(active.getId(), productId);
        linkService(active.getId(), serviceItemId);

        createActivity("过期活动", "ACTIVE",
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1),
                "/uploads/images/expired.jpg");
        createActivity("草稿活动", "DRAFT",
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(7),
                "/uploads/images/draft.jpg");

        mockMvc.perform(get("/api/v1/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("当前活动"))
                .andExpect(jsonPath("$.data.items[?(@.title=='过期活动')]").doesNotExist())
                .andExpect(jsonPath("$.data.items[?(@.title=='草稿活动')]").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].coverUrl").value("/uploads/images/current.jpg"))
                .andExpect(jsonPath("$.data.items[0].products[0].id").value(productId.toString()))
                .andExpect(jsonPath("$.data.items[0].services[0].id").value(serviceItemId.toString()));

        mockMvc.perform(get("/api/v1/activities/" + active.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.coverUrl").value("/uploads/images/current.jpg"))
                .andExpect(jsonPath("$.data.products[0].name").value("活动零食"))
                .andExpect(jsonPath("$.data.services[0].name").value("活动洗护"));
    }

    private MarketingActivity createActivity(String title, String status, LocalDateTime startTime,
                                             LocalDateTime endTime, String coverUrl) {
        MarketingActivity activity = new MarketingActivity();
        activity.setTitle(title);
        activity.setActivityType("MIXED");
        activity.setDescription(title + "描述");
        activity.setStartTime(startTime);
        activity.setEndTime(endTime);
        activity.setCoverUrl(coverUrl);
        activity.setStatus(status);
        marketingActivityService.save(activity);
        return activity;
    }

    private void linkProduct(Long activityId, Long linkedProductId) {
        ActivityProduct link = new ActivityProduct();
        link.setActivityId(activityId);
        link.setProductId(linkedProductId);
        activityProductService.save(link);
    }

    private void linkService(Long activityId, Long linkedServiceItemId) {
        ActivityService link = new ActivityService();
        link.setActivityId(activityId);
        link.setServiceItemId(linkedServiceItemId);
        activityServiceService.save(link);
    }

    private String createAdmin(String username, String roleCode, String... permissions) {
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode("password123456"));
        admin.setRole(roleCode);
        admin.setStatus("ACTIVE");
        adminUserService.save(admin);

        AdminRole role = new AdminRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);
        role.setStatus("ACTIVE");
        adminRoleService.save(role);

        for (String code : permissions) {
            AdminPermission permission = new AdminPermission();
            permission.setPermissionCode(code);
            permission.setPermissionName(code);
            permission.setModule(code.substring(0, code.indexOf(':')));
            permission.setStatus("ACTIVE");
            adminPermissionService.save(permission);

            AdminRolePermission relation = new AdminRolePermission();
            relation.setRoleId(role.getId());
            relation.setPermissionId(permission.getId());
            adminRolePermissionService.save(relation);
        }
        return jwtTokenService.signAdminToken(admin.getId(), username, roleCode);
    }

    private String bearer(String jwt) {
        return "Bearer " + jwt;
    }
}
