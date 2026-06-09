package com.petcare.product.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.product.dto.CartItemCheckRequest;
import com.petcare.product.dto.CartItemCreateRequest;
import com.petcare.product.dto.CartItemResponse;
import com.petcare.product.dto.CartItemUpdateRequest;
import com.petcare.product.service.CartApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User-facing cart endpoints.
 * currentUserId comes from the security context, not the request body.
 * Returns 401 if no user identity is available.
 */
@RestController
@RequestMapping("/api/v1/cart-items")
public class CartController {

    private final CartApplicationService cartService;

    public CartController(CartApplicationService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> listCartItems() {
        Long currentUserId = resolveCurrentUserId();
        List<CartItemResponse> items = cartService.listCartItems(currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartItemResponse>> addCartItem(
            @Valid @RequestBody CartItemCreateRequest request) {
        Long currentUserId = resolveCurrentUserId();
        CartItemResponse response = cartService.addCartItem(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @PathVariable Long id,
            @Valid @RequestBody CartItemUpdateRequest request) {
        Long currentUserId = resolveCurrentUserId();
        CartItemResponse response = cartService.updateCartItem(currentUserId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(@PathVariable Long id) {
        Long currentUserId = resolveCurrentUserId();
        cartService.deleteCartItem(currentUserId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> checkCartItems(
            @Valid @RequestBody CartItemCheckRequest request) {
        Long currentUserId = resolveCurrentUserId();
        List<CartItemResponse> items = cartService.checkCartItems(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    /**
     * Resolves current user ID from the security context.
     * Returns 401 if no user identity is available.
     */
    private Long resolveCurrentUserId() {
        throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "用户端购物车功能暂未开放，请等待用户登录功能上线");
    }
}
