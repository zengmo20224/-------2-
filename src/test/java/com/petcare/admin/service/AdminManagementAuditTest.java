package com.petcare.admin.service;

import com.petcare.admin.entity.AdminOperationLog;
import com.petcare.admin.service.impl.AdminManagementServiceImpl;
import com.petcare.booking.service.StaffScheduleService;
import com.petcare.product.entity.Product;
import com.petcare.product.service.ProductCategoryService;
import com.petcare.product.service.ProductService;
import com.petcare.service.service.ServiceCategoryService;
import com.petcare.service.service.ServiceItemService;
import com.petcare.staff.service.StaffService;
import com.petcare.staff.service.StaffSkillService;
import com.petcare.store.service.StoreConfigService;
import com.petcare.store.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminManagementAuditTest {

    @Mock private StoreService storeService;
    @Mock private StoreConfigService storeConfigService;
    @Mock private ServiceCategoryService serviceCategoryService;
    @Mock private ServiceItemService serviceItemService;
    @Mock private StaffService staffService;
    @Mock private StaffSkillService staffSkillService;
    @Mock private StaffScheduleService staffScheduleService;
    @Mock private ProductCategoryService productCategoryService;
    @Mock private ProductService productService;
    @Mock private AdminOperationLogService operationLogService;

    private AdminManagementServiceImpl service;
    private Product product;

    @BeforeEach
    void setUp() {
        service = new AdminManagementServiceImpl(
                storeService, storeConfigService, serviceCategoryService, serviceItemService,
                staffService, staffSkillService, staffScheduleService, productCategoryService,
                productService, operationLogService);
        product = new Product();
        product.setId(100L);
        product.setStock(10);
        product.setStatus("ON_SALE");
        when(productService.getOne(any(), eq(false))).thenReturn(product);
    }

    @Test
    void requiredSuccessAuditFailurePropagatesAndCreatesSafeFailLog() {
        when(productService.updateById(product)).thenReturn(true);
        when(operationLogService.save(any(AdminOperationLog.class))).thenReturn(false);

        assertThatThrownBy(() -> service.updateProductStock(100L, 36, 900L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required admin operation log");

        ArgumentCaptor<AdminOperationLog> failLog = ArgumentCaptor.forClass(AdminOperationLog.class);
        verify(operationLogService).saveFailLog(failLog.capture());
        assertThat(failLog.getValue().getResult()).isEqualTo("FAIL");
        assertThat(failLog.getValue().getErrorMessage()).isEqualTo("unexpected_error");
    }

    @Test
    void unexpectedBusinessFailureCreatesSanitizedFailLog() {
        doThrow(new RuntimeException("SELECT secret FROM product"))
                .when(productService).updateById(product);

        assertThatThrownBy(() -> service.updateProductStock(100L, 36, 900L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SELECT secret");

        ArgumentCaptor<AdminOperationLog> failLog = ArgumentCaptor.forClass(AdminOperationLog.class);
        verify(operationLogService).saveFailLog(failLog.capture());
        assertThat(failLog.getValue().getResult()).isEqualTo("FAIL");
        assertThat(failLog.getValue().getErrorMessage()).isEqualTo("unexpected_error");
    }
}
