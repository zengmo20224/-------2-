package com.petcare.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.pagination.PageResponse;
import com.petcare.product.dto.ProductOrderCreateRequest;
import com.petcare.product.dto.ProductOrderDetailResponse;
import com.petcare.product.dto.ProductOrderDetailResponse.OrderItemResponse;
import com.petcare.product.dto.ProductOrderResponse;
import com.petcare.product.entity.ProductOrder;
import com.petcare.product.entity.ProductOrderItem;
import com.petcare.product.mapper.ProductOrderItemMapper;
import com.petcare.product.mapper.ProductOrderMapper;
import com.petcare.product.service.ProductOrderApplicationService;
import com.petcare.product.service.ProductOrderTransactionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * User-facing product order application service.
 * Orchestrates transaction service calls and converts entities to DTOs.
 */
@Service
public class ProductOrderApplicationServiceImpl implements ProductOrderApplicationService {

    private final ProductOrderMapper orderMapper;
    private final ProductOrderItemMapper orderItemMapper;
    private final ProductOrderTransactionService transactionService;

    public ProductOrderApplicationServiceImpl(
            ProductOrderMapper orderMapper,
            ProductOrderItemMapper orderItemMapper,
            ProductOrderTransactionService transactionService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.transactionService = transactionService;
    }

    @Override
    public ProductOrderResponse createOrder(Long currentUserId, ProductOrderCreateRequest request) {
        ProductOrder order = transactionService.createOrder(currentUserId, request);
        return toOrderResponse(order);
    }

    @Override
    public PageResponse<ProductOrderResponse> getMyOrders(Long currentUserId, int page, int size) {
        LambdaQueryWrapper<ProductOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductOrder::getUserId, currentUserId)
               .eq(ProductOrder::getDeleted, 0)
               .orderByDesc(ProductOrder::getCreateTime);

        IPage<ProductOrder> result = orderMapper.selectPage(new Page<>(page, size), wrapper);

        List<ProductOrderResponse> items = result.getRecords().stream()
                .map(this::toOrderResponse)
                .toList();
        return PageResponse.of(items, result.getTotal(), page, size);
    }

    @Override
    public ProductOrderDetailResponse getOrderDetail(Long currentUserId, Long orderId) {
        ProductOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_NOT_FOUND, "订单不存在");
        }
        if (!order.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_FORBIDDEN, "无权查看此订单");
        }
        return toOrderDetailResponse(order);
    }

    @Override
    public ProductOrderResponse cancelOrder(Long currentUserId, Long orderId) {
        ProductOrder order = transactionService.cancelOrder(orderId, currentUserId);
        return toOrderResponse(order);
    }

    private ProductOrderResponse toOrderResponse(ProductOrder order) {
        return new ProductOrderResponse(
                order.getId(), order.getOrderNo(), order.getTotalAmount(),
                order.getDeliveryMethod(), order.getAddressSnapshot(),
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
                order.getDeliveryMethod(), order.getAddressSnapshot(),
                order.getPaymentMethod(), order.getPaymentStatus(),
                order.getPickupStatus(), order.getStatus(),
                order.getContactName(), order.getContactPhone(),
                order.getRemark(), order.getMerchantRemark(),
                order.getCreateTime(), order.getConfirmTime(),
                order.getCompleteTime(), order.getCancelTime(),
                itemResponses);
    }
}
