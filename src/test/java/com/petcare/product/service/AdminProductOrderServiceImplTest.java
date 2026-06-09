package com.petcare.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.petcare.admin.entity.AdminOperationLog;
import com.petcare.admin.service.AdminOperationLogService;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.product.dto.ProductOrderResponse;
import com.petcare.product.entity.ProductOrder;
import com.petcare.product.enums.PickupStatus;
import com.petcare.product.enums.ProductOrderStatus;
import com.petcare.product.mapper.ProductOrderItemMapper;
import com.petcare.product.mapper.ProductOrderMapper;
import com.petcare.product.service.impl.AdminProductOrderServiceImpl;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link AdminProductOrderServiceImpl}.
 * Focuses on verifying admin operation log writing for all 6 actions.
 */
@ExtendWith(MockitoExtension.class)
class AdminProductOrderServiceImplTest {

    @Mock
    private ProductOrderMapper orderMapper;

    @Mock
    private ProductOrderItemMapper orderItemMapper;

    @Mock
    private ProductOrderTransactionService transactionService;

    @Mock
    private AdminOperationLogService operationLogService;

    @InjectMocks
    private AdminProductOrderServiceImpl adminService;

    private static final Long ORDER_ID = 6001L;
    private static final Long OPERATOR_ID = 9001L;

    private ProductOrder defaultOrder;

    @BeforeEach
    void setUp() {
        defaultOrder = new ProductOrder();
        defaultOrder.setId(ORDER_ID);
        defaultOrder.setOrderNo("PO20260601100001");
        defaultOrder.setUserId(1001L);
        defaultOrder.setStoreId(5001L);
        defaultOrder.setTotalAmount(new BigDecimal("198.00"));
        defaultOrder.setPaymentMethod("OFFLINE_STORE");
        defaultOrder.setPaymentStatus("UNPAID");
        defaultOrder.setPickupStatus(PickupStatus.WAIT_PREPARE.getCode());
        defaultOrder.setStatus(ProductOrderStatus.PENDING_CONFIRM.getCode());
        defaultOrder.setContactName("张三");
        defaultOrder.setContactPhone("13800000000");
        defaultOrder.setDeleted(0);
    }

    // ==================== confirmOrder ====================

    @Nested
    @DisplayName("confirmOrder audit log")
    class ConfirmOrderLogTests {

        @Test
        @DisplayName("successful confirm writes success log")
        void confirmOrder_successLog() {
            // Arrange
            defaultOrder.setStatus(ProductOrderStatus.PREPARING.getCode());
            doReturn(defaultOrder).when(transactionService).confirmOrder(ORDER_ID, OPERATOR_ID);
            doReturn(true).when(operationLogService).save(any(AdminOperationLog.class));

            // Act
            adminService.confirmOrder(ORDER_ID, OPERATOR_ID);

            // Assert — verify log was written with correct fields
            ArgumentCaptor<AdminOperationLog> logCaptor = ArgumentCaptor.forClass(AdminOperationLog.class);
            verify(operationLogService).save(logCaptor.capture());

            AdminOperationLog captured = logCaptor.getValue();
            assertThat(captured.getAdminId()).isEqualTo(OPERATOR_ID);
            assertThat(captured.getModule()).isEqualTo("product_order");
            assertThat(captured.getOperation()).isEqualTo("confirm");
            assertThat(captured.getRequestMethod()).isEqualTo("POST");
            assertThat(captured.getRequestUrl()).contains(String.valueOf(ORDER_ID));
            assertThat(captured.getRequestUrl()).contains("confirm");
            assertThat(captured.getResult()).isEqualTo("success");
            assertThat(captured.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("failed confirm writes fail log and re-throws")
        void confirmOrder_failLog() {
            // Arrange
            doThrow(new BusinessException(ErrorCode.PRODUCT_ORDER_STATUS_INVALID, "状态不允许"))
                    .when(transactionService).confirmOrder(ORDER_ID, OPERATOR_ID);
            doReturn(true).when(operationLogService).save(any(AdminOperationLog.class));

            // Act & Assert
            assertThatThrownBy(() -> adminService.confirmOrder(ORDER_ID, OPERATOR_ID))
                    .isInstanceOf(BusinessException.class);

            ArgumentCaptor<AdminOperationLog> logCaptor = ArgumentCaptor.forClass(AdminOperationLog.class);
            verify(operationLogService).save(logCaptor.capture());

            AdminOperationLog captured = logCaptor.getValue();
            assertThat(captured.getResult()).isEqualTo("fail");
            assertThat(captured.getErrorMessage()).isEqualTo("状态不允许");
        }
    }

    // ==================== markReadyForPickup ====================

    @Nested
    @DisplayName("markReadyForPickup audit log")
    class MarkReadyLogTests {

        @Test
        @DisplayName("successful ready writes success log")
        void markReady_successLog() {
            // Arrange
            defaultOrder.setStatus(ProductOrderStatus.READY_FOR_PICKUP.getCode());
            doReturn(defaultOrder).when(transactionService).markReadyForPickup(ORDER_ID, OPERATOR_ID);
            doReturn(true).when(operationLogService).save(any(AdminOperationLog.class));

            // Act
            adminService.markReadyForPickup(ORDER_ID, OPERATOR_ID);

            // Assert
            ArgumentCaptor<AdminOperationLog> logCaptor = ArgumentCaptor.forClass(AdminOperationLog.class);
            verify(operationLogService).save(logCaptor.capture());

            AdminOperationLog captured = logCaptor.getValue();
            assertThat(captured.getOperation()).isEqualTo("ready");
            assertThat(captured.getResult()).isEqualTo("success");
        }
    }

    // ==================== confirmPayment ====================

    @Nested
    @DisplayName("confirmPayment audit log")
    class ConfirmPaymentLogTests {

        @Test
        @DisplayName("successful payment confirmation writes success log")
        void confirmPayment_successLog() {
            // Arrange
            defaultOrder.setPaymentStatus("OFFLINE_PAID");
            defaultOrder.setPickupStatus(PickupStatus.PICKED_UP.getCode());
            doReturn(defaultOrder).when(transactionService).confirmPayment(ORDER_ID, OPERATOR_ID);
            doReturn(true).when(operationLogService).save(any(AdminOperationLog.class));

            // Act
            adminService.confirmPayment(ORDER_ID, OPERATOR_ID);

            // Assert
            ArgumentCaptor<AdminOperationLog> logCaptor = ArgumentCaptor.forClass(AdminOperationLog.class);
            verify(operationLogService).save(logCaptor.capture());

            AdminOperationLog captured = logCaptor.getValue();
            assertThat(captured.getOperation()).isEqualTo("confirm-payment");
            assertThat(captured.getResult()).isEqualTo("success");
        }

        @Test
        @DisplayName("duplicate payment confirmation writes fail log and re-throws")
        void confirmPayment_duplicateFailLog() {
            // Arrange
            doThrow(new BusinessException(ErrorCode.PRODUCT_ORDER_STATUS_INVALID, "订单已确认收款，不能重复确认"))
                    .when(transactionService).confirmPayment(ORDER_ID, OPERATOR_ID);
            doReturn(true).when(operationLogService).save(any(AdminOperationLog.class));

            // Act & Assert
            assertThatThrownBy(() -> adminService.confirmPayment(ORDER_ID, OPERATOR_ID))
                    .isInstanceOf(BusinessException.class);

            ArgumentCaptor<AdminOperationLog> logCaptor = ArgumentCaptor.forClass(AdminOperationLog.class);
            verify(operationLogService).save(logCaptor.capture());

            AdminOperationLog captured = logCaptor.getValue();
            assertThat(captured.getResult()).isEqualTo("fail");
            assertThat(captured.getErrorMessage()).contains("重复确认");
        }
    }

    // ==================== completeOrder ====================

    @Nested
    @DisplayName("completeOrder audit log")
    class CompleteOrderLogTests {

        @Test
        @DisplayName("successful complete writes success log")
        void completeOrder_successLog() {
            // Arrange
            defaultOrder.setStatus(ProductOrderStatus.COMPLETED.getCode());
            doReturn(defaultOrder).when(transactionService).completeOrder(ORDER_ID, OPERATOR_ID);
            doReturn(true).when(operationLogService).save(any(AdminOperationLog.class));

            // Act
            adminService.completeOrder(ORDER_ID, OPERATOR_ID);

            // Assert
            ArgumentCaptor<AdminOperationLog> logCaptor = ArgumentCaptor.forClass(AdminOperationLog.class);
            verify(operationLogService).save(logCaptor.capture());

            AdminOperationLog captured = logCaptor.getValue();
            assertThat(captured.getOperation()).isEqualTo("complete");
            assertThat(captured.getResult()).isEqualTo("success");
        }
    }

    // ==================== cancelOrder ====================

    @Nested
    @DisplayName("cancelOrder audit log")
    class CancelOrderLogTests {

        @Test
        @DisplayName("successful cancel writes success log")
        void cancelOrder_successLog() {
            // Arrange
            defaultOrder.setStatus(ProductOrderStatus.CANCELLED.getCode());
            doReturn(defaultOrder).when(transactionService).adminCancelOrder(ORDER_ID, "原因", OPERATOR_ID);
            doReturn(true).when(operationLogService).save(any(AdminOperationLog.class));

            // Act
            adminService.cancelOrder(ORDER_ID, "原因", OPERATOR_ID);

            // Assert
            ArgumentCaptor<AdminOperationLog> logCaptor = ArgumentCaptor.forClass(AdminOperationLog.class);
            verify(operationLogService).save(logCaptor.capture());

            AdminOperationLog captured = logCaptor.getValue();
            assertThat(captured.getOperation()).isEqualTo("cancel");
            assertThat(captured.getResult()).isEqualTo("success");
        }

        @Test
        @DisplayName("failed cancel writes fail log and re-throws")
        void cancelOrder_failLog() {
            // Arrange
            doThrow(new BusinessException(ErrorCode.PRODUCT_ORDER_STATUS_INVALID, "已付款订单不能取消"))
                    .when(transactionService).adminCancelOrder(ORDER_ID, "原因", OPERATOR_ID);
            doReturn(true).when(operationLogService).save(any(AdminOperationLog.class));

            // Act & Assert
            assertThatThrownBy(() -> adminService.cancelOrder(ORDER_ID, "原因", OPERATOR_ID))
                    .isInstanceOf(BusinessException.class);

            ArgumentCaptor<AdminOperationLog> logCaptor = ArgumentCaptor.forClass(AdminOperationLog.class);
            verify(operationLogService).save(logCaptor.capture());

            AdminOperationLog captured = logCaptor.getValue();
            assertThat(captured.getResult()).isEqualTo("fail");
            assertThat(captured.getErrorMessage()).contains("不能取消");
        }
    }

    // ==================== outOfStock ====================

    @Nested
    @DisplayName("outOfStock audit log")
    class OutOfStockLogTests {

        @Test
        @DisplayName("successful out-of-stock writes success log")
        void outOfStock_successLog() {
            // Arrange
            defaultOrder.setStatus(ProductOrderStatus.OUT_OF_STOCK.getCode());
            doReturn(defaultOrder).when(transactionService).outOfStock(ORDER_ID, "缺货", OPERATOR_ID);
            doReturn(true).when(operationLogService).save(any(AdminOperationLog.class));

            // Act
            adminService.outOfStock(ORDER_ID, "缺货", OPERATOR_ID);

            // Assert
            ArgumentCaptor<AdminOperationLog> logCaptor = ArgumentCaptor.forClass(AdminOperationLog.class);
            verify(operationLogService).save(logCaptor.capture());

            AdminOperationLog captured = logCaptor.getValue();
            assertThat(captured.getOperation()).isEqualTo("out-of-stock");
            assertThat(captured.getResult()).isEqualTo("success");
        }
    }

    // ==================== Log write failure tolerance ====================

    @Nested
    @DisplayName("Log write failure tolerance")
    class LogFailureToleranceTests {

        @Test
        @DisplayName("log service failure does NOT prevent business operation from succeeding")
        void logFailure_doesNotAffectBusiness() {
            // Arrange
            defaultOrder.setStatus(ProductOrderStatus.PREPARING.getCode());
            doReturn(defaultOrder).when(transactionService).confirmOrder(ORDER_ID, OPERATOR_ID);
            doThrow(new RuntimeException("DB connection lost"))
                    .when(operationLogService).save(any(AdminOperationLog.class));

            // Act — should NOT throw even though logging failed
            ProductOrderResponse result = adminService.confirmOrder(ORDER_ID, OPERATOR_ID);

            // Assert — business result is still returned
            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(ProductOrderStatus.PREPARING.getCode());
        }
    }
}
