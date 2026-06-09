package com.petcare.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.pagination.PageResponse;
import com.petcare.product.dto.ProductOrderDetailResponse;
import com.petcare.product.dto.ProductOrderDetailResponse.OrderItemResponse;
import com.petcare.product.dto.ProductOrderResponse;
import com.petcare.product.entity.ProductOrder;
import com.petcare.product.entity.ProductOrderItem;
import com.petcare.product.mapper.ProductOrderItemMapper;
import com.petcare.product.mapper.ProductOrderMapper;
import com.petcare.product.service.AdminProductOrderService;
import com.petcare.product.service.ProductOrderTransactionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Admin product order application service.
 * No ownership check — admin can access any order.
 */
@Service
public class AdminProductOrderServiceImpl implements AdminProductOrderService {

    private final ProductOrderMapper orderMapper;
    private final ProductOrderItemMapper orderItemMapper;
    private final ProductOrderTransactionService transactionService;

    public AdminProductOrderServiceImpl(
            ProductOrderMapper orderMapper,
            ProductOrderItemMapper orderItemMapper,
            ProductOrderTransactionService transactionService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.transactionService = transactionService;
    }

    @Override
    public PageResponse<ProductOrderResponse> listOrders(int page, int size, String status) {
        LambdaQueryWrapper<ProductOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductOrder::getDeleted, 0);
        if (status != null && !status.isBlank()) {
            wrapper.eq(ProductOrder::getStatus, status);
        }
        wrapper.orderByDesc(ProductOrder::getCreateTime);

        IPage<ProductOrder> result = orderMapper.selectPage(new Page<>(page, size), wrapper);

        List<ProductOrderResponse> items = result.getRecords().stream()
                .map(this::toOrderResponse)
                .toList();
        return PageResponse.of(items, result.getTotal(), page, size);
    }

    @Override
    public ProductOrderDetailResponse getOrderDetail(Long orderId) {
        ProductOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_NOT_FOUND, "订单不存在");
        }
        return toOrderDetailResponse(order);
    }

    @Override
    public ProductOrderResponse confirmOrder(Long orderId, Long operatorId) {
        ProductOrder order = transactionService.confirmOrder(orderId, operatorId);
        return toOrderResponse(order);
    }

    @Override
    public ProductOrderResponse markReadyForPickup(Long orderId, Long operatorId) {
        ProductOrder order = transactionService.markReadyForPickup(orderId, operatorId);
        return toOrderResponse(order);
    }

    @Override
    public ProductOrderResponse confirmPayment(Long orderId, Long operatorId) {
        ProductOrder order = transactionService.confirmPayment(orderId, operatorId);
        return toOrderResponse(order);
    }

    @Override
    public ProductOrderResponse completeOrder(Long orderId, Long operatorId) {
        ProductOrder order = transactionService.completeOrder(orderId, operatorId);
        return toOrderResponse(order);
    }

    @Override
    public ProductOrderResponse cancelOrder(Long orderId, String reason, Long operatorId) {
        ProductOrder order = transactionService.adminCancelOrder(orderId, reason, operatorId);
        return toOrderResponse(order);
    }

    @Override
    public ProductOrderResponse outOfStock(Long orderId, String reason, Long operatorId) {
        ProductOrder order = transactionService.outOfStock(orderId, reason, operatorId);
        return toOrderResponse(order);
    }

    private ProductOrderResponse toOrderResponse(ProductOrder order) {
        return new ProductOrderResponse(
                order.getId(), order.getOrderNo(), order.getTotalAmount(),
                order.getPaymentMethod(), order.getPaymentStatus(),
                order.getPickupStatus(), order.getStatus(),
                order.getContactName(), order.getContactPhone(), order.getRemark(),
                order.getCreateTime(), order.getConfirmTime(),
                order.getCompleteTime(), order.getCancelTime());
    }

    private ProductOrderDetailResponse toOrderDetailResponse(ProductOrder order) {
        List<ProductOrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> new OrderItemResponse(
                        item.getId(), item.getProductId(),
                        item.getProductName(), item.getProductCoverUrl(),
                        item.getPrice(), item.getQuantity(), item.getTotalAmount()))
                .toList();

        return new ProductOrderDetailResponse(
                order.getId(), order.getOrderNo(), order.getUserId(),
                order.getStoreId(), order.getTotalAmount(),
                order.getPaymentMethod(), order.getPaymentStatus(),
                order.getPickupStatus(), order.getStatus(),
                order.getContactName(), order.getContactPhone(),
                order.getRemark(), order.getMerchantRemark(),
                order.getCreateTime(), order.getConfirmTime(),
                order.getCompleteTime(), order.getCancelTime(),
                itemResponses);
    }
}
