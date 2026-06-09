package com.petcare.product.controller;

import com.petcare.admin.entity.AdminPermission;
import com.petcare.admin.entity.AdminRole;
import com.petcare.admin.entity.AdminRolePermission;
import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.service.AdminPermissionService;
import com.petcare.admin.service.AdminRolePermissionService;
import com.petcare.admin.service.AdminRoleService;
import com.petcare.admin.service.AdminUserService;
import com.petcare.common.security.JwtTokenService;
import com.petcare.product.entity.Product;
import com.petcare.product.entity.ProductCategory;
import com.petcare.product.entity.ProductImage;
import com.petcare.product.service.ProductCategoryService;
import com.petcare.product.service.ProductImageService;
import com.petcare.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
 * Integration tests for {@link ProductCatalogController}.
 * Catalog endpoints have no @PreAuthorize annotations but still require authentication
 * per SecurityConfig (all /api/v1/** are authenticated).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService productImageService;

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
    private Long catId;
    private Long productId;

    @BeforeEach
    void setUp() {
        // Setup authenticated admin for catalog browsing
        Long adminId = createTestAdmin("catalog_tester", "password123456", "VIEWER");
        setupRoleAndPermission("VIEWER", "product:catalog:read", "product");
        authToken = jwtTokenService.signAdminToken(adminId, "catalog_tester", "VIEWER");

        // Create active category
        ProductCategory cat = new ProductCategory();
        cat.setName("猫粮_catalogTest");
        cat.setIconUrl("https://example.com/icon.png");
        cat.setSort(1);
        cat.setStatus("ACTIVE");
        productCategoryService.save(cat);
        catId = cat.getId();

        // Create inactive category (should be excluded from listing)
        ProductCategory inactiveCat = new ProductCategory();
        inactiveCat.setName("下架分类_catalogTest");
        inactiveCat.setIconUrl("https://example.com/icon2.png");
        inactiveCat.setSort(2);
        inactiveCat.setStatus("INACTIVE");
        productCategoryService.save(inactiveCat);

        // Create on-sale product
        Product product = new Product();
        product.setCategoryId(catId);
        product.setName("皇家猫粮_catalogTest");
        product.setCoverUrl("https://example.com/cover.jpg");
        product.setPrice(new BigDecimal("128.00"));
        product.setStock(50);
        product.setSalesCount(10);
        product.setDescription("优质猫粮");
        product.setPickupOnly(1);
        product.setStatus("ON_SALE");
        product.setSort(1);
        productService.save(product);
        productId = product.getId();

        // Create product images
        ProductImage img1 = new ProductImage();
        img1.setProductId(productId);
        img1.setImageUrl("https://example.com/img1.jpg");
        img1.setSort(1);
        productImageService.save(img1);

        ProductImage img2 = new ProductImage();
        img2.setProductId(productId);
        img2.setImageUrl("https://example.com/img2.jpg");
        img2.setSort(2);
        productImageService.save(img2);

        // Create off-sale product (should be excluded from listing)
        Product offSale = new Product();
        offSale.setCategoryId(catId);
        offSale.setName("下架商品_catalogTest");
        offSale.setCoverUrl("https://example.com/cover2.jpg");
        offSale.setPrice(new BigDecimal("50.00"));
        offSale.setStock(0);
        offSale.setSalesCount(0);
        offSale.setDescription("已下架");
        offSale.setPickupOnly(1);
        offSale.setStatus("OFF_SALE");
        offSale.setSort(2);
        productService.save(offSale);

        // Create product in another category for filtering test
        ProductCategory cat2 = new ProductCategory();
        cat2.setName("狗粮_catalogTest");
        cat2.setIconUrl("https://example.com/icon3.png");
        cat2.setSort(3);
        cat2.setStatus("ACTIVE");
        productCategoryService.save(cat2);

        Product dogFood = new Product();
        dogFood.setCategoryId(cat2.getId());
        dogFood.setName("皇家狗粮_catalogTest");
        dogFood.setCoverUrl("https://example.com/cover3.jpg");
        dogFood.setPrice(new BigDecimal("168.00"));
        dogFood.setStock(30);
        dogFood.setSalesCount(5);
        dogFood.setDescription("优质狗粮");
        dogFood.setPickupOnly(1);
        dogFood.setStatus("ON_SALE");
        dogFood.setSort(1);
        productService.save(dogFood);
    }

    // ==================== List Categories ====================

    @Nested
    @DisplayName("GET /api/v1/product-categories")
    class ListCategoriesTests {

        @Test
        @DisplayName("returns 200 with active categories")
        void listCategoriesReturnsActive() throws Exception {
            mockMvc.perform(get("/api/v1/product-categories")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[?(@.name=='猫粮_catalogTest')]").exists())
                    .andExpect(jsonPath("$.data[?(@.name=='狗粮_catalogTest')]").exists())
                    .andExpect(jsonPath("$.data[?(@.name=='下架分类_catalogTest')]").doesNotExist());
        }
    }

    // ==================== List Products ====================

    @Nested
    @DisplayName("GET /api/v1/products")
    class ListProductsTests {

        @Test
        @DisplayName("returns 200 with on-sale products")
        void listProductsReturnsOnSale() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.items").isArray())
                    .andExpect(jsonPath("$.data.items[?(@.name=='皇家猫粮_catalogTest')]").exists())
                    .andExpect(jsonPath("$.data.items[?(@.name=='皇家狗粮_catalogTest')]").exists())
                    .andExpect(jsonPath("$.data.items[?(@.name=='下架商品_catalogTest')]").doesNotExist());
        }

        @Test
        @DisplayName("filters by categoryId")
        void filterByCategoryId() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("categoryId", catId.toString())
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.items.length()").value(1))
                    .andExpect(jsonPath("$.data.items[0].name").value("皇家猫粮_catalogTest"));
        }
    }

    // ==================== Get Product Detail ====================

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class GetProductDetailTests {

        @Test
        @DisplayName("for existing product returns 200 with detail")
        void getExistingProduct() throws Exception {
            mockMvc.perform(get("/api/v1/products/" + productId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(productId))
                    .andExpect(jsonPath("$.data.name").value("皇家猫粮_catalogTest"))
                    .andExpect(jsonPath("$.data.price").value(128.00))
                    .andExpect(jsonPath("$.data.categoryName").value("猫粮_catalogTest"))
                    .andExpect(jsonPath("$.data.description").value("优质猫粮"))
                    .andExpect(jsonPath("$.data.imageUrls").isArray())
                    .andExpect(jsonPath("$.data.imageUrls.length()").value(2));
        }

        @Test
        @DisplayName("for non-existing product returns 404")
        void getNonExistingProduct() throws Exception {
            mockMvc.perform(get("/api/v1/products/999999999")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("product_not_found"));
        }
    }

    // ==================== Helper Methods ====================

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
