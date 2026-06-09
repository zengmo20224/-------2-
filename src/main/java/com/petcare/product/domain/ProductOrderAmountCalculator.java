package com.petcare.product.domain;

import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Pure domain rule for calculating order amounts.
 * Uses BigDecimal with 2 decimal places. Never trusts client-side prices.
 */
public final class ProductOrderAmountCalculator {

    private ProductOrderAmountCalculator() {
        // utility class
    }

    /**
     * Calculates the subtotal for a single order line item.
     *
     * @param price    the server-side product price
     * @param quantity the purchase quantity
     * @return subtotal = price × quantity, scaled to 2 decimal places
     * @throws BusinessException if quantity is not positive or price is negative
     */
    public static BigDecimal calculateLineTotal(BigDecimal price, int quantity) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_ORDER_AMOUNT_INVALID,
                    "商品价格不合法");
        }
        if (quantity <= 0) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_ORDER_AMOUNT_INVALID,
                    "购买数量必须为正整数");
        }
        return price.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the total order amount from a list of line totals.
     *
     * @param lineTotals list of per-item subtotals
     * @return sum of all line totals, scaled to 2 decimal places
     * @throws BusinessException if the list is empty
     */
    public static BigDecimal calculateOrderTotal(List<BigDecimal> lineTotals) {
        if (lineTotals == null || lineTotals.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.CART_NO_CHECKED_ITEMS,
                    "没有已选中的购物车项");
        }
        return lineTotals.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Record representing a single order line snapshot.
     *
     * @param productId       product ID
     * @param productName     product name snapshot
     * @param productCoverUrl product cover URL snapshot
     * @param price           unit price snapshot
     * @param quantity        purchase quantity
     * @param totalAmount     line subtotal
     */
    public record LineSnapshot(
            Long productId,
            String productName,
            String productCoverUrl,
            BigDecimal price,
            int quantity,
            BigDecimal totalAmount
    ) {
    }
}
