package com.petcare.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.product.dto.CartItemCheckRequest;
import com.petcare.product.dto.CartItemCreateRequest;
import com.petcare.product.dto.CartItemResponse;
import com.petcare.product.dto.CartItemUpdateRequest;
import com.petcare.product.entity.CartItem;
import com.petcare.product.entity.Product;
import com.petcare.product.mapper.CartItemMapper;
import com.petcare.product.mapper.ProductMapper;
import com.petcare.product.service.CartApplicationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of cart operations.
 * Uses upsert to prevent duplicate cart rows for the same user+product.
 */
@Service
public class CartApplicationServiceImpl implements CartApplicationService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    public CartApplicationServiceImpl(CartItemMapper cartItemMapper, ProductMapper productMapper) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
    }

    @Override
    public CartItemResponse addCartItem(Long currentUserId, CartItemCreateRequest request) {
        // Validate product exists, is on sale, and not deleted
        Product product = productMapper.selectById(request.productId());
        if (product == null || product.getDeleted() == 1
                || !"ON_SALE".equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_ON_SALE, "商品不存在或已下架");
        }

        // Upsert: insert or increase quantity if already exists.
        // The custom @Insert SQL bypasses MyBatis-Plus ASSIGN_ID auto-fill, so we
        // must generate the snowflake id explicitly (same pattern as savePostImages).
        // On a duplicate (user_id, product_id) the id is ignored by ON DUPLICATE KEY UPDATE.
        cartItemMapper.upsert(IdWorker.getId(), currentUserId, request.productId(), request.quantity());

        // Read back the cart item to return
        CartItem item = findCartItemByUserAndProduct(currentUserId, request.productId());
        return toCartItemResponse(item, product);
    }

    @Override
    public CartItemResponse updateCartItem(Long currentUserId, Long cartItemId,
                                           CartItemUpdateRequest request) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        validateCartItemOwnership(item, cartItemId, currentUserId);

        cartItemMapper.updateQuantityByIdAndUser(cartItemId, currentUserId, request.quantity());

        // Re-read to get updated values
        CartItem updated = cartItemMapper.selectById(cartItemId);
        Product product = productMapper.selectById(updated.getProductId());
        return toCartItemResponse(updated, product);
    }

    @Override
    public List<CartItemResponse> checkCartItems(Long currentUserId, CartItemCheckRequest request) {
        int checked = Boolean.TRUE.equals(request.checked()) ? 1 : 0;

        LambdaUpdateWrapper<CartItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CartItem::getUserId, currentUserId)
               .in(CartItem::getId, request.cartItemIds())
               .set(CartItem::getChecked, checked);
        cartItemMapper.update(null, wrapper);

        return listCartItems(currentUserId);
    }

    @Override
    public void deleteCartItem(Long currentUserId, Long cartItemId) {
        int rows = cartItemMapper.deleteByIdAndUser(cartItemId, currentUserId);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND, "购物车项不存在或无权操作");
        }
    }

    @Override
    public List<CartItemResponse> listCartItems(Long currentUserId) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, currentUserId)
               .orderByDesc(CartItem::getUpdateTime);

        List<CartItem> items = cartItemMapper.selectList(wrapper);
        return items.stream()
                .map(item -> {
                    Product product = productMapper.selectById(item.getProductId());
                    return toCartItemResponse(item, product);
                })
                .toList();
    }

    private CartItem findCartItemByUserAndProduct(Long userId, Long productId) {
        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId)
               .eq(CartItem::getProductId, productId);
        return cartItemMapper.selectOne(wrapper);
    }

    private void validateCartItemOwnership(CartItem item, Long cartItemId, Long currentUserId) {
        if (item == null || !item.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.CART_ITEM_FORBIDDEN, "购物车项不存在或无权操作");
        }
    }

    private CartItemResponse toCartItemResponse(CartItem item, Product product) {
        BigDecimal price = product != null ? product.getPrice() : BigDecimal.ZERO;
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        return new CartItemResponse(
                item.getId(), item.getProductId(),
                product != null ? product.getName() : "商品已下架",
                product != null ? product.getCoverUrl() : null,
                price,
                product != null ? product.getStock() : 0,
                item.getQuantity(),
                item.getChecked() == 1,
                subtotal);
    }
}
