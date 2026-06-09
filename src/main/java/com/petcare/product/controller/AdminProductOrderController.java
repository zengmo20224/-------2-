package com.petcare.product.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.pagination.PageResponse;
import com.petcare.common.security.SecurityContextHelper;
import com.petcare.product.dto.AdminOrderActionRequest;
import com.petcare.product.dto.ProductOrderDetailResponse;
import com.petcare.product.dto.ProductOrderResponse;
import com.petcare.product.service.AdminProductOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin product order management endpoints.
 * All endpoints require authentication and specific permission codes.
 */
@RestController
@RequestMapping("/api/v1/admin/product-orders")
public class AdminProductOrderController {

    private final AdminProductOrderService adminOrderService;

    public AdminProductOrderController(AdminProductOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('product:order:read')")
    public ResponseEntity<ApiResponse<PageResponse<ProductOrderResponse>>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        PageResponse<ProductOrderResponse> response = adminOrderService.listOrders(page, size, status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product:order:read')")
    public ResponseEntity<ApiResponse<ProductOrderDetailResponse>> getOrderDetail(@PathVariable Long id) {
        ProductOrderDetailResponse response = adminOrderService.getOrderDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('product:order:confirm')")
    public ResponseEntity<ApiResponse<ProductOrderResponse>> confirmOrder(@PathVariable Long id) {
        Long operatorId = resolveOperatorId();
        ProductOrderResponse response = adminOrderService.confirmOrder(id, operatorId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/ready")
    @PreAuthorize("hasAuthority('product:order:ready')")
    public ResponseEntity<ApiResponse<ProductOrderResponse>> markReadyForPickup(@PathVariable Long id) {
        Long operatorId = resolveOperatorId();
        ProductOrderResponse response = adminOrderService.markReadyForPickup(id, operatorId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/confirm-payment")
    @PreAuthorize("hasAuthority('product:order:confirm-payment')")
    public ResponseEntity<ApiResponse<ProductOrderResponse>> confirmPayment(@PathVariable Long id) {
        Long operatorId = resolveOperatorId();
        ProductOrderResponse response = adminOrderService.confirmPayment(id, operatorId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('product:order:complete')")
    public ResponseEntity<ApiResponse<ProductOrderResponse>> completeOrder(@PathVariable Long id) {
        Long operatorId = resolveOperatorId();
        ProductOrderResponse response = adminOrderService.completeOrder(id, operatorId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('product:order:cancel')")
    public ResponseEntity<ApiResponse<ProductOrderResponse>> cancelOrder(
            @PathVariable Long id,
            @RequestBody(required = false) AdminOrderActionRequest request) {
        Long operatorId = resolveOperatorId();
        String reason = request != null ? request.reason() : null;
        ProductOrderResponse response = adminOrderService.cancelOrder(id, reason, operatorId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/out-of-stock")
    @PreAuthorize("hasAuthority('product:order:cancel')")
    public ResponseEntity<ApiResponse<ProductOrderResponse>> outOfStock(
            @PathVariable Long id,
            @RequestBody(required = false) AdminOrderActionRequest request) {
        Long operatorId = resolveOperatorId();
        String reason = request != null ? request.reason() : null;
        ProductOrderResponse response = adminOrderService.outOfStock(id, reason, operatorId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    private Long resolveOperatorId() {
        return SecurityContextHelper.getCurrentAdminId()
                .orElseThrow(() -> new IllegalStateException("No admin identity"));
    }
}
