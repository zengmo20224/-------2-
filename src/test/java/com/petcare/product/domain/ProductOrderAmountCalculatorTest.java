package com.petcare.product.domain;

import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for ProductOrderAmountCalculator.
 * Tests pure domain logic with no Spring context.
 */
class ProductOrderAmountCalculatorTest {

    @Nested
    @DisplayName("calculateLineTotal")
    class CalculateLineTotal {

        @Test
        @DisplayName("calculates single item subtotal correctly")
        void calculatesSingleItemSubtotal() {
            BigDecimal result = ProductOrderAmountCalculator.calculateLineTotal(
                    new BigDecimal("29.90"), 3);
            assertThat(result).isEqualByComparingTo(new BigDecimal("89.70"));
            assertThat(result.scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("calculates subtotal for quantity of 1")
        void calculatesForQuantityOne() {
            BigDecimal result = ProductOrderAmountCalculator.calculateLineTotal(
                    new BigDecimal("150.00"), 1);
            assertThat(result).isEqualByComparingTo(new BigDecimal("150.00"));
        }

        @Test
        @DisplayName("calculates subtotal for large quantity")
        void calculatesForLargeQuantity() {
            BigDecimal result = ProductOrderAmountCalculator.calculateLineTotal(
                    new BigDecimal("0.01"), 99);
            assertThat(result).isEqualByComparingTo(new BigDecimal("0.99"));
        }

        @Test
        @DisplayName("scales result to exactly 2 decimal places")
        void scalesToTwoDecimalPlaces() {
            // 19.99 * 3 = 59.97
            BigDecimal result = ProductOrderAmountCalculator.calculateLineTotal(
                    new BigDecimal("19.99"), 3);
            assertThat(result).isEqualByComparingTo(new BigDecimal("59.97"));
            assertThat(result.scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("throws for zero quantity")
        void throwsForZeroQuantity() {
            assertThatThrownBy(() -> ProductOrderAmountCalculator.calculateLineTotal(
                    new BigDecimal("10.00"), 0))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_AMOUNT_INVALID);
        }

        @Test
        @DisplayName("throws for negative quantity")
        void throwsForNegativeQuantity() {
            assertThatThrownBy(() -> ProductOrderAmountCalculator.calculateLineTotal(
                    new BigDecimal("10.00"), -1))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_AMOUNT_INVALID);
        }

        @Test
        @DisplayName("throws for negative price")
        void throwsForNegativePrice() {
            assertThatThrownBy(() -> ProductOrderAmountCalculator.calculateLineTotal(
                    new BigDecimal("-5.00"), 2))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_AMOUNT_INVALID);
        }

        @Test
        @DisplayName("throws for null price")
        void throwsForNullPrice() {
            assertThatThrownBy(() -> ProductOrderAmountCalculator.calculateLineTotal(
                    null, 2))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_AMOUNT_INVALID);
        }

        @Test
        @DisplayName("accepts zero price")
        void acceptsZeroPrice() {
            BigDecimal result = ProductOrderAmountCalculator.calculateLineTotal(
                    BigDecimal.ZERO, 5);
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.scale()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("calculateOrderTotal")
    class CalculateOrderTotal {

        @Test
        @DisplayName("sums multiple line totals correctly")
        void sumsMultipleLineTotals() {
            List<BigDecimal> lines = List.of(
                    new BigDecimal("89.70"),
                    new BigDecimal("150.00"),
                    new BigDecimal("29.90"));
            BigDecimal result = ProductOrderAmountCalculator.calculateOrderTotal(lines);
            assertThat(result).isEqualByComparingTo(new BigDecimal("269.60"));
            assertThat(result.scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("handles single line total")
        void handlesSingleLineTotal() {
            List<BigDecimal> lines = List.of(new BigDecimal("99.99"));
            BigDecimal result = ProductOrderAmountCalculator.calculateOrderTotal(lines);
            assertThat(result).isEqualByComparingTo(new BigDecimal("99.99"));
        }

        @Test
        @DisplayName("throws for empty list")
        void throwsForEmptyList() {
            assertThatThrownBy(() -> ProductOrderAmountCalculator.calculateOrderTotal(
                    Collections.emptyList()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.CART_NO_CHECKED_ITEMS);
        }

        @Test
        @DisplayName("throws for null list")
        void throwsForNullList() {
            assertThatThrownBy(() -> ProductOrderAmountCalculator.calculateOrderTotal(null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.CART_NO_CHECKED_ITEMS);
        }

        @Test
        @DisplayName("client-provided prices are never trusted — calculation uses server data")
        void clientPricesNeverTrusted() {
            // Simulate server-side calculation ignoring any client input
            BigDecimal serverPrice = new BigDecimal("29.90");
            int quantity = 3;
            BigDecimal lineTotal = ProductOrderAmountCalculator.calculateLineTotal(serverPrice, quantity);
            BigDecimal orderTotal = ProductOrderAmountCalculator.calculateOrderTotal(List.of(lineTotal));

            // Client might send 999.99 as total, but server calculates correctly
            assertThat(orderTotal).isEqualByComparingTo(new BigDecimal("89.70"));
            assertThat(new BigDecimal("999.99")).isNotEqualByComparingTo(orderTotal);
        }
    }
}
