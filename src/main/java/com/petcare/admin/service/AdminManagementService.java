package com.petcare.admin.service;

import com.petcare.admin.dto.AdminManagementDtos.OperationLogView;
import com.petcare.admin.dto.AdminManagementDtos.ProductRequest;
import com.petcare.admin.dto.AdminManagementDtos.ProductView;
import com.petcare.admin.dto.AdminManagementDtos.ServiceItemRequest;
import com.petcare.admin.dto.AdminManagementDtos.ServiceItemView;
import com.petcare.admin.dto.AdminManagementDtos.StaffRequest;
import com.petcare.admin.dto.AdminManagementDtos.StaffScheduleRequest;
import com.petcare.admin.dto.AdminManagementDtos.StaffScheduleView;
import com.petcare.admin.dto.AdminManagementDtos.StaffSkillView;
import com.petcare.admin.dto.AdminManagementDtos.StaffView;
import com.petcare.admin.dto.AdminManagementDtos.StoreConfigUpdateRequest;
import com.petcare.admin.dto.AdminManagementDtos.StoreConfigView;
import com.petcare.admin.dto.AdminManagementDtos.StoreUpdateRequest;
import com.petcare.admin.dto.AdminManagementDtos.StoreView;
import com.petcare.common.pagination.PageResponse;
import java.util.List;

public interface AdminManagementService {

    StoreView getStore(Long id);
    StoreView updateStore(Long id, StoreUpdateRequest request, Long operatorId);
    StoreConfigView getStoreConfig(Long storeId);
    StoreConfigView updateStoreConfig(Long storeId, StoreConfigUpdateRequest request, Long operatorId);

    PageResponse<ServiceItemView> listServiceItems(int page, int size, String status);
    ServiceItemView createServiceItem(ServiceItemRequest request, Long operatorId);
    ServiceItemView updateServiceItem(Long id, ServiceItemRequest request, Long operatorId);
    ServiceItemView disableServiceItem(Long id, Long operatorId);

    PageResponse<StaffView> listStaff(int page, int size, String status);
    StaffView createStaff(StaffRequest request, Long operatorId);
    StaffView updateStaff(Long id, StaffRequest request, Long operatorId);
    StaffView disableStaff(Long id, Long operatorId);
    StaffSkillView replaceStaffSkills(Long staffId, List<Long> categoryIds, Long operatorId);
    PageResponse<StaffScheduleView> listSchedules(Long staffId, int page, int size);
    StaffScheduleView createSchedule(Long staffId, StaffScheduleRequest request, Long operatorId);
    StaffScheduleView updateSchedule(Long staffId, Long scheduleId, StaffScheduleRequest request, Long operatorId);

    PageResponse<ProductView> listProducts(int page, int size, String status);
    ProductView createProduct(ProductRequest request, Long operatorId);
    ProductView updateProduct(Long id, ProductRequest request, Long operatorId);
    ProductView disableProduct(Long id, Long operatorId);
    ProductView updateProductStock(Long id, Integer stock, Long operatorId);

    PageResponse<OperationLogView> listOperationLogs(int page, int size, String module);
}
