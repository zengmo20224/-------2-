package com.petcare.product.domain;

import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for ProductOrderStateMachine.
 * Tests all allowed and forbidden state transitions.
 */
class ProductOrderStateMachineTest {

    @Nested
    @DisplayName("Allowed transitions")
    class AllowedTransitions {

        @Test
        @DisplayName("null -> PENDING_CONFIRM is allowed (initial creation)")
        void nullToPendingConfirm() {
            assertThatCode(() -> ProductOrderStateMachine.validateTransition(
                    null, "PENDING_CONFIRM"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PENDING_CONFIRM -> PREPARING is allowed (confirm order)")
        void pendingConfirmToPreparing() {
            assertThatCode(() -> ProductOrderStateMachine.validateTransition(
                    "PENDING_CONFIRM", "PREPARING"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PENDING_CONFIRM -> CANCELLED is allowed (user cancel)")
        void pendingConfirmToCancelled() {
            assertThatCode(() -> ProductOrderStateMachine.validateTransition(
                    "PENDING_CONFIRM", "CANCELLED"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PENDING_CONFIRM -> OUT_OF_STOCK is allowed")
        void pendingConfirmToOutOfStock() {
            assertThatCode(() -> ProductOrderStateMachine.validateTransition(
                    "PENDING_CONFIRM", "OUT_OF_STOCK"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PREPARING -> READY_FOR_PICKUP is allowed (ready)")
        void preparingToReady() {
            assertThatCode(() -> ProductOrderStateMachine.validateTransition(
                    "PREPARING", "READY_FOR_PICKUP"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PREPARING -> CANCELLED is allowed")
        void preparingToCancelled() {
            assertThatCode(() -> ProductOrderStateMachine.validateTransition(
                    "PREPARING", "CANCELLED"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("READY_FOR_PICKUP -> COMPLETED is allowed (complete)")
        void readyToCompleted() {
            assertThatCode(() -> ProductOrderStateMachine.validateTransition(
                    "READY_FOR_PICKUP", "COMPLETED"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("READY_FOR_PICKUP -> CANCELLED is allowed")
        void readyToCancelled() {
            assertThatCode(() -> ProductOrderStateMachine.validateTransition(
                    "READY_FOR_PICKUP", "CANCELLED"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Forbidden transitions")
    class ForbiddenTransitions {

        @ParameterizedTest(name = "{0} -> {1} is forbidden")
        @DisplayName("Terminal states cannot transition")
        @CsvSource({
                "COMPLETED, PREPARING",
                "COMPLETED, CANCELLED",
                "COMPLETED, OUT_OF_STOCK",
                "CANCELLED, PENDING_CONFIRM",
                "CANCELLED, PREPARING",
                "CANCELLED, COMPLETED",
                "OUT_OF_STOCK, PENDING_CONFIRM",
                "OUT_OF_STOCK, PREPARING",
                "OUT_OF_STOCK, CANCELLED"
        })
        void terminalStatesCannotTransition(String from, String to) {
            assertThatThrownBy(() -> ProductOrderStateMachine.validateTransition(from, to))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_STATUS_INVALID);
        }

        @ParameterizedTest(name = "{0} -> {1} is forbidden")
        @DisplayName("Non-adjacent transitions are forbidden")
        @CsvSource({
                "PENDING_CONFIRM, COMPLETED",
                "PENDING_CONFIRM, READY_FOR_PICKUP",
                "PREPARING, COMPLETED",
                "PREPARING, OUT_OF_STOCK",
                "PREPARING, PENDING_CONFIRM",
                "READY_FOR_PICKUP, PREPARING",
                "READY_FOR_PICKUP, PENDING_CONFIRM",
                "READY_FOR_PICKUP, OUT_OF_STOCK"
        })
        void nonAdjacentTransitionsForbidden(String from, String to) {
            assertThatThrownBy(() -> ProductOrderStateMachine.validateTransition(from, to))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_STATUS_INVALID);
        }

        @Test
        @DisplayName("null -> anything other than PENDING_CONFIRM is forbidden")
        void nullToNonPendingConfirmForbidden() {
            assertThatThrownBy(() -> ProductOrderStateMachine.validateTransition(null, "COMPLETED"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_STATUS_INVALID);
        }
    }

    @Nested
    @DisplayName("isTerminalStatus")
    class IsTerminalStatus {

        @Test
        @DisplayName("COMPLETED is terminal")
        void completedIsTerminal() {
            assertThat(ProductOrderStateMachine.isTerminalStatus("COMPLETED")).isTrue();
        }

        @Test
        @DisplayName("CANCELLED is terminal")
        void cancelledIsTerminal() {
            assertThat(ProductOrderStateMachine.isTerminalStatus("CANCELLED")).isTrue();
        }

        @Test
        @DisplayName("OUT_OF_STOCK is terminal")
        void outOfStockIsTerminal() {
            assertThat(ProductOrderStateMachine.isTerminalStatus("OUT_OF_STOCK")).isTrue();
        }

        @Test
        @DisplayName("PENDING_CONFIRM is not terminal")
        void pendingConfirmIsNotTerminal() {
            assertThat(ProductOrderStateMachine.isTerminalStatus("PENDING_CONFIRM")).isFalse();
        }

        @Test
        @DisplayName("PREPARING is not terminal")
        void preparingIsNotTerminal() {
            assertThat(ProductOrderStateMachine.isTerminalStatus("PREPARING")).isFalse();
        }

        @Test
        @DisplayName("READY_FOR_PICKUP is not terminal")
        void readyIsNotTerminal() {
            assertThat(ProductOrderStateMachine.isTerminalStatus("READY_FOR_PICKUP")).isFalse();
        }
    }

    @Nested
    @DisplayName("validateCanComplete")
    class ValidateCanComplete {

        @Test
        @DisplayName("succeeds when READY_FOR_PICKUP + OFFLINE_PAID + PICKED_UP")
        void succeedsWhenAllConditionsMet() {
            assertThatCode(() -> ProductOrderStateMachine.validateCanComplete(
                    "READY_FOR_PICKUP", "OFFLINE_PAID", "PICKED_UP"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("fails when not READY_FOR_PICKUP")
        void failsWhenNotReady() {
            assertThatThrownBy(() -> ProductOrderStateMachine.validateCanComplete(
                    "PREPARING", "OFFLINE_PAID", "PICKED_UP"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_STATUS_INVALID);
        }

        @Test
        @DisplayName("fails when not OFFLINE_PAID")
        void failsWhenNotPaid() {
            assertThatThrownBy(() -> ProductOrderStateMachine.validateCanComplete(
                    "READY_FOR_PICKUP", "UNPAID", "PICKED_UP"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_PAYMENT_REQUIRED);
        }

        @Test
        @DisplayName("fails when not PICKED_UP")
        void failsWhenNotPickedUp() {
            assertThatThrownBy(() -> ProductOrderStateMachine.validateCanComplete(
                    "READY_FOR_PICKUP", "OFFLINE_PAID", "READY_FOR_PICKUP"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_PICKUP_REQUIRED);
        }
    }

    @Nested
    @DisplayName("validateCanCancel")
    class ValidateCanCancel {

        @Test
        @DisplayName("succeeds when UNPAID and not picked up")
        void succeedsWhenUnpaid() {
            assertThatCode(() -> ProductOrderStateMachine.validateCanCancel(
                    "UNPAID", "WAIT_PREPARE"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("fails when already OFFLINE_PAID")
        void failsWhenPaid() {
            assertThatThrownBy(() -> ProductOrderStateMachine.validateCanCancel(
                    "OFFLINE_PAID", "WAIT_PREPARE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_STATUS_INVALID);
        }

        @Test
        @DisplayName("fails when already PICKED_UP")
        void failsWhenPickedUp() {
            assertThatThrownBy(() -> ProductOrderStateMachine.validateCanCancel(
                    "UNPAID", "PICKED_UP"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_STATUS_INVALID);
        }
    }

    @Nested
    @DisplayName("validateCanConfirmPayment")
    class ValidateCanConfirmPayment {

        @Test
        @DisplayName("succeeds when READY_FOR_PICKUP")
        void succeedsWhenReady() {
            assertThatCode(() -> ProductOrderStateMachine.validateCanConfirmPayment(
                    "READY_FOR_PICKUP"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("fails when PREPARING")
        void failsWhenPreparing() {
            assertThatThrownBy(() -> ProductOrderStateMachine.validateCanConfirmPayment(
                    "PREPARING"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_STATUS_INVALID);
        }

        @Test
        @DisplayName("fails when PENDING_CONFIRM")
        void failsWhenPendingConfirm() {
            assertThatThrownBy(() -> ProductOrderStateMachine.validateCanConfirmPayment(
                    "PENDING_CONFIRM"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.PRODUCT_ORDER_STATUS_INVALID);
        }
    }
}
