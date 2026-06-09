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
import com.petcare.product.entity.ProductOrder;
import com.petcare.product.entity.ProductOrderItem;
import com.petcare.product.mapper.ProductOrderItemMapper;
import com.petcare.product.mapper.ProductOrderMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link AdminProductOrderController}.
 * Uses full Spring context with H2 database to test authentication,
 * authorization, and endpoint behavior end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminProductOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private ProductOrderMapper orderMapper;

    @Autowired
    private ProductOrderItemMapper orderItemMapper;

    private String adminToken;
    private String noPermToken;
    private Long pendingConfirmOrderId;
    private Long preparingOrderId;
    private Long readyForPickupOrderId;
    private Long paidPickedUpOrderId;

    @BeforeEach
    void setUp() {
        // Create admin with all product order permissions
        Long adminId = createTestAdmin("order_admin", "password123456", "ORDER_MGR");
        setupOrderPermissions("ORDER_MGR");
        adminToken = jwtTokenService.signAdminToken(adminId, "order_admin", "ORDER_MGR");

        // Create admin with unrelated permissions (no product:order:*)
        Long noPermAdminId = createTestAdmin("no_order_admin", "password123456", "BOOKING_MGR");
        setupNoOrderPermissions("BOOKING_MGR");
        noPermToken = jwtTokenService.signAdminToken(noPermAdminId, "no_order_admin", "BOOKING_MGR");

        // Create test orders in various states
        pendingConfirmOrderId = createTestOrder("PO20260601001", "PENDING_CONFIRM", "UNPAID", "WAIT_PREPARE");
        preparingOrderId = createTestOrder("PO20260601002", "PREPARING", "UNPAID", "WAIT_PREPARE");
        readyForPickupOrderId = createTestOrder("PO20260601003", "READY_FOR_PICKUP", "UNPAID", "READY_FOR_PICKUP");
        paidPickedUpOrderId = createTestOrder("PO20260601004", "READY_FOR_PICKUP", "OFFLINE_PAID", "PICKED_UP");
    }

    // ==================== List Orders ====================

    @Nested
    @DisplayName("GET /api/v1/admin/product-orders")
    class ListOrdersTests {

        @Test
        @DisplayName("with product:order:read permission returns 200")
        void listOrdersWithPermission() throws Exception {
            mockMvc.perform(get("/api/v1/admin/product-orders")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.items").isArray())
                    .andExpect(jsonPath("$.data.items.length()").value(4));
        }

        @Test
        @DisplayName("without authentication returns 401")
        void listOrdersUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/admin/product-orders"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("without product:order:read permission returns 403")
        void listOrdersWithoutPermission() throws Exception {
            mockMvc.perform(get("/api/v1/admin/product-orders")
                            .header("Authorization", "Bearer " + noPermToken))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== Get Order Detail ====================

    @Nested
    @DisplayName("GET /api/v1/admin/product-orders/{id}")
    class GetOrderDetailTests {

        @Test
        @DisplayName("for existing order returns 200 with detail")
        void getExistingOrder() throws Exception {
            mockMvc.perform(get("/api/v1/admin/product-orders/" + pendingConfirmOrderId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(pendingConfirmOrderId))
                    .andExpect(jsonPath("$.data.orderNo").value("PO20260601001"))
                    .andExpect(jsonPath("$.data.status").value("PENDING_CONFIRM"))
                    .andExpect(jsonPath("$.data.items").isArray());
        }

        @Test
        @DisplayName("for non-existing order returns 404")
        void getNonExistingOrder() throws Exception {
            mockMvc.perform(get("/api/v1/admin/product-orders/999999999")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("product_order_not_found"));
        }
    }

    // ==================== Confirm Order ====================

    @Nested
    @DisplayName("POST /api/v1/admin/product-orders/{id}/confirm")
    class ConfirmOrderTests {

        @Test
        @DisplayName("PENDING_CONFIRM -> PREPARING returns 200")
        void confirmPendingOrder() throws Exception {
            mockMvc.perform(post("/api/v1/admin/product-orders/" + pendingConfirmOrderId + "/confirm")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(pendingConfirmOrderId))
                    .andExpect(jsonPath("$.data.status").value("PREPARING"));
        }
    }

    // ==================== Mark Ready For Pickup ====================

    @Nested
    @DisplayName("POST /api/v1/admin/product-orders/{id}/ready")
    class MarkReadyTests {

        @Test
        @DisplayName("PREPARING -> READY_FOR_PICKUP returns 200")
        void markPreparingOrderReady() throws Exception {
            mockMvc.perform(post("/api/v1/admin/product-orders/" + preparingOrderId + "/ready")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(preparingOrderId))
                    .andExpect(jsonPath("$.data.status").value("READY_FOR_PICKUP"));
        }
    }

    // ==================== Confirm Payment ====================

    @Nested
    @DisplayName("POST /api/v1/admin/product-orders/{id}/confirm-payment")
    class ConfirmPaymentTests {

        @Test
        @DisplayName("READY_FOR_PICKUP -> payment confirmed returns 200")
        void confirmPaymentForReadyOrder() throws Exception {
            mockMvc.perform(post("/api/v1/admin/product-orders/" + readyForPickupOrderId + "/confirm-payment")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(readyForPickupOrderId))
                    .andExpect(jsonPath("$.data.paymentStatus").value("OFFLINE_PAID"));
        }
    }

    // ==================== Complete Order ====================

    @Nested
    @DisplayName("POST /api/v1/admin/product-orders/{id}/complete")
    class CompleteOrderTests {

        @Test
        @DisplayName("paid and picked-up order -> COMPLETED returns 200")
        void completePaidPickedUpOrder() throws Exception {
            mockMvc.perform(post("/api/v1/admin/product-orders/" + paidPickedUpOrderId + "/complete")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(paidPickedUpOrderId))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }
    }

    // ==================== Cancel Order ====================

    @Nested
    @DisplayName("POST /api/v1/admin/product-orders/{id}/cancel")
    class CancelOrderTests {

        @Test
        @DisplayName("PENDING_CONFIRM -> CANCELLED returns 200")
        void cancelOrder() throws Exception {
            mockMvc.perform(post("/api/v1/admin/product-orders/" + pendingConfirmOrderId + "/cancel")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"reason\":\"库存不足取消\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(pendingConfirmOrderId))
                    .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }
    }

    // ==================== Out Of Stock ====================

    @Nested
    @DisplayName("POST /api/v1/admin/product-orders/{id}/out-of-stock")
    class OutOfStockTests {

        @Test
        @DisplayName("PENDING_CONFIRM -> OUT_OF_STOCK returns 200")
        void markOutOfStock() throws Exception {
            mockMvc.perform(post("/api/v1/admin/product-orders/" + pendingConfirmOrderId + "/out-of-stock")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"reason\":\"商品已无库存\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(pendingConfirmOrderId))
                    .andExpect(jsonPath("$.data.status").value("OUT_OF_STOCK"));
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

    private void setupOrderPermissions(String roleCode) {
        AdminRole role = new AdminRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);
        role.setStatus("ACTIVE");
        adminRoleService.save(role);

        String[] permCodes = {
                "product:order:read",
                "product:order:confirm",
                "product:order:ready",
                "product:order:confirm-payment",
                "product:order:complete",
                "product:order:cancel"
        };
        for (String code : permCodes) {
            AdminPermission perm = new AdminPermission();
            perm.setPermissionCode(code);
            perm.setPermissionName(code);
            perm.setModule("product");
            perm.setStatus("ACTIVE");
            adminPermissionService.save(perm);

            AdminRolePermission rp = new AdminRolePermission();
            rp.setRoleId(role.getId());
            rp.setPermissionId(perm.getId());
            adminRolePermissionService.save(rp);
        }
    }

    private void setupNoOrderPermissions(String roleCode) {
        AdminRole role = new AdminRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);
        role.setStatus("ACTIVE");
        adminRoleService.save(role);

        // Grant booking permissions but NOT product order permissions
        AdminPermission perm = new AdminPermission();
        perm.setPermissionCode("booking:booking:read");
        perm.setPermissionName("booking:booking:read");
        perm.setModule("booking");
        perm.setStatus("ACTIVE");
        adminPermissionService.save(perm);

        AdminRolePermission rp = new AdminRolePermission();
        rp.setRoleId(role.getId());
        rp.setPermissionId(perm.getId());
        adminRolePermissionService.save(rp);
    }

    private Long createTestOrder(String orderNo, String status, String paymentStatus, String pickupStatus) {
        ProductOrder order = new ProductOrder();
        order.setOrderNo(orderNo);
        order.setUserId(9001L);
        order.setStoreId(1L);
        order.setTotalAmount(new BigDecimal("199.00"));
        order.setPaymentMethod("OFFLINE_STORE");
        order.setPaymentStatus(paymentStatus);
        order.setPickupStatus(pickupStatus);
        order.setStatus(status);
        order.setContactName("测试联系人");
        order.setContactPhone("13800000001");
        orderMapper.insert(order);

        // Create an order item for each order
        ProductOrderItem item = new ProductOrderItem();
        item.setOrderId(order.getId());
        item.setProductId(100L);
        item.setProductName("测试商品_" + orderNo);
        item.setProductCoverUrl("https://example.com/cover.jpg");
        item.setPrice(new BigDecimal("199.00"));
        item.setQuantity(1);
        item.setTotalAmount(new BigDecimal("199.00"));
        orderItemMapper.insert(item);

        return order.getId();
    }
}
