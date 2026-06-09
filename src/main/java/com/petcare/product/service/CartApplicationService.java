package com.petcare.product.service;

import com.petcare.product.dto.CartItemCheckRequest;
import com.petcare.product.dto.CartItemCreateRequest;
import com.petcare.product.dto.CartItemResponse;
import com.petcare.product.dto.CartItemUpdateRequest;

import java.util.List;

/**
 * Application service for cart operations.
 * currentUserId always comes from the security context, not the request body.
 */
public interface CartApplicationService {

    /**
     * Adds a product to the cart. Upserts if same user+product already exists.
     */
    CartItemResponse addCartItem(Long currentUserId, CartItemCreateRequest request);

    /**
     * Updates cart item quantity.
     */
    CartItemResponse updateCartItem(Long currentUserId, Long cartItemId, CartItemUpdateRequest request);

    /**
     * Sets checked status on cart items.
     */
    List<CartItemResponse> checkCartItems(Long currentUserId, CartItemCheckRequest request);

    /**
     * Deletes a cart item.
     */
    void deleteCartItem(Long currentUserId, Long cartItemId);

    /**
     * Lists all cart items for the current user.
     */
    List<CartItemResponse> listCartItems(Long currentUserId);
}
