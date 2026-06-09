package com.petcare.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.petcare.admin.entity.AdminOperationLog;
import com.petcare.admin.service.AdminManagementService;
import com.petcare.admin.service.AdminOperationLogService;
import com.petcare.booking.entity.StaffSchedule;
import com.petcare.booking.service.StaffScheduleService;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.pagination.PageResponse;
import com.petcare.product.entity.Product;
import com.petcare.product.entity.ProductCategory;
import com.petcare.product.service.ProductCategoryService;
import com.petcare.product.service.ProductService;
import com.petcare.service.entity.ServiceCategory;
import com.petcare.service.entity.ServiceItem;
import com.petcare.service.service.ServiceCategoryService;
import com.petcare.service.service.ServiceItemService;
import com.petcare.staff.entity.Staff;
import com.petcare.staff.entity.StaffSkill;
import com.petcare.staff.service.StaffService;
import com.petcare.staff.service.StaffSkillService;
import com.petcare.store.entity.Store;
import com.petcare.store.entity.StoreConfig;
import com.petcare.store.service.StoreConfigService;
import com.petcare.store.service.StoreService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminManagementServiceImpl implements AdminManagementService {

    private final StoreService storeService;
    private final StoreConfigService storeConfigService;
    private final ServiceCategoryService serviceCategoryService;
    private final ServiceItemService serviceItemService;
    private final StaffService staffService;
    private final StaffSkillService staffSkillService;
    private final StaffScheduleService scheduleService;
    private final ProductCategoryService productCategoryService;
    private final ProductService productService;
    private final AdminOperationLogService operationLogService;

    public AdminManagementServiceImpl(StoreService storeService, StoreConfigService storeConfigService,
            ServiceCategoryService serviceCategoryService, ServiceItemService serviceItemService,
            StaffService staffService, StaffSkillService staffSkillService,
            StaffScheduleService scheduleService, ProductCategoryService productCategoryService,
            ProductService productService,
            AdminOperationLogService operationLogService) {
        this.storeService = storeService;
        this.storeConfigService = storeConfigService;
        this.serviceCategoryService = serviceCategoryService;
        this.serviceItemService = serviceItemService;
        this.staffService = staffService;
        this.staffSkillService = staffSkillService;
        this.scheduleService = scheduleService;
        this.productCategoryService = productCategoryService;
        this.productService = productService;
        this.operationLogService = operationLogService;
    }

    @Override
    public StoreView getStore(Long id) {
        return storeView(requireStore(id));
    }

    @Override
    @Transactional
    public StoreView updateStore(Long id, StoreUpdateRequest request, Long operatorId) {
        Store store = requireStore(id);
        store.setStoreName(request.storeName());
        store.setPhone(request.phone());
        store.setAddress(request.address());
        store.setLongitude(request.longitude());
        store.setLatitude(request.latitude());
        store.setBusinessHours(request.businessHours());
        store.setStatus(request.status());
        store.setDescription(request.description());
        storeService.updateById(store);
        audit(operatorId, "store", "update-info", "PATCH", "/api/v1/admin/stores/" + id);
        return storeView(store);
    }

    @Override
    public StoreConfigView getStoreConfig(Long storeId) {
        return storeConfigView(requireStoreConfig(storeId));
    }

    @Override
    @Transactional
    public StoreConfigView updateStoreConfig(Long storeId, StoreConfigUpdateRequest request, Long operatorId) {
        requireStore(storeId);
        StoreConfig config = requireStoreConfig(storeId);
        config.setHomeServiceRadiusKm(request.homeServiceRadiusKm());
        config.setBookingAdvanceDays(request.bookingAdvanceDays());
        config.setBookingCancelHours(request.bookingCancelHours());
        config.setTimeSlotMinutes(request.timeSlotMinutes());
        config.setAutoConfirmBooking(flag(request.autoConfirmBooking()));
        config.setContentAutoPublish(flag(request.contentAutoPublish()));
        storeConfigService.updateById(config);
        audit(operatorId, "store", "update-config", "PUT", "/api/v1/admin/stores/" + storeId + "/config");
        return storeConfigView(config);
    }

    @Override
    public PageResponse<ServiceItemView> listServiceItems(int page, int size, String status) {
        LambdaQueryWrapper<ServiceItem> query = new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getDeleted, 0)
                .eq(hasText(status), ServiceItem::getStatus, status)
                .orderByAsc(ServiceItem::getSort).orderByDesc(ServiceItem::getCreateTime);
        IPage<ServiceItem> result = serviceItemService.page(page(page, size), query);
        return response(result, page, size, this::serviceItemView);
    }

    @Override
    @Transactional
    public ServiceItemView createServiceItem(ServiceItemRequest request, Long operatorId) {
        requireServiceCategory(request.categoryId());
        ServiceItem item = new ServiceItem();
        apply(item, request);
        item.setStatus("ON_SALE");
        serviceItemService.save(item);
        audit(operatorId, "service", "create-item", "POST", "/api/v1/admin/service-items");
        return serviceItemView(item);
    }

    @Override
    @Transactional
    public ServiceItemView updateServiceItem(Long id, ServiceItemRequest request, Long operatorId) {
        requireServiceCategory(request.categoryId());
        ServiceItem item = requireServiceItem(id);
        apply(item, request);
        serviceItemService.updateById(item);
        audit(operatorId, "service", "update-item", "PUT", "/api/v1/admin/service-items/" + id);
        return serviceItemView(item);
    }

    @Override
    @Transactional
    public ServiceItemView disableServiceItem(Long id, Long operatorId) {
        ServiceItem item = requireServiceItem(id);
        item.setStatus("OFF_SALE");
        serviceItemService.updateById(item);
        audit(operatorId, "service", "disable-item", "POST", "/api/v1/admin/service-items/" + id + "/disable");
        return serviceItemView(item);
    }

    @Override
    public PageResponse<StaffView> listStaff(int page, int size, String status) {
        LambdaQueryWrapper<Staff> query = new LambdaQueryWrapper<Staff>()
                .eq(Staff::getDeleted, 0)
                .eq(hasText(status), Staff::getStatus, status)
                .orderByDesc(Staff::getCreateTime);
        IPage<Staff> result = staffService.page(page(page, size), query);
        return response(result, page, size, this::staffView);
    }

    @Override
    @Transactional
    public StaffView createStaff(StaffRequest request, Long operatorId) {
        requireStore(request.storeId());
        Staff staff = new Staff();
        apply(staff, request);
        staff.setStatus("ACTIVE");
        staffService.save(staff);
        audit(operatorId, "staff", "create-profile", "POST", "/api/v1/admin/staff");
        return staffView(staff);
    }

    @Override
    @Transactional
    public StaffView updateStaff(Long id, StaffRequest request, Long operatorId) {
        requireStore(request.storeId());
        Staff staff = requireStaff(id);
        apply(staff, request);
        staffService.updateById(staff);
        audit(operatorId, "staff", "update-profile", "PUT", "/api/v1/admin/staff/" + id);
        return staffView(staff);
    }

    @Override
    @Transactional
    public StaffView disableStaff(Long id, Long operatorId) {
        Staff staff = requireStaff(id);
        staff.setStatus("INACTIVE");
        staffService.updateById(staff);
        audit(operatorId, "staff", "disable-profile", "POST", "/api/v1/admin/staff/" + id + "/disable");
        return staffView(staff);
    }

    @Override
    @Transactional
    public StaffSkillView replaceStaffSkills(Long staffId, List<Long> categoryIds, Long operatorId) {
        requireStaff(staffId);
        List<Long> distinctIds = categoryIds.stream().distinct().toList();
        distinctIds.forEach(this::requireServiceCategory);
        staffSkillService.remove(new LambdaQueryWrapper<StaffSkill>().eq(StaffSkill::getStaffId, staffId));
        distinctIds.forEach(categoryId -> {
            StaffSkill skill = new StaffSkill();
            skill.setStaffId(staffId);
            skill.setServiceCategoryId(categoryId);
            staffSkillService.save(skill);
        });
        audit(operatorId, "staff", "replace-skills", "PUT", "/api/v1/admin/staff/" + staffId + "/skills");
        return new StaffSkillView(staffId, distinctIds);
    }

    @Override
    public PageResponse<StaffScheduleView> listSchedules(Long staffId, int page, int size) {
        requireStaff(staffId);
        LambdaQueryWrapper<StaffSchedule> query = new LambdaQueryWrapper<StaffSchedule>()
                .eq(StaffSchedule::getStaffId, staffId)
                .eq(StaffSchedule::getDeleted, 0)
                .orderByDesc(StaffSchedule::getWorkDate).orderByAsc(StaffSchedule::getStartTime);
        IPage<StaffSchedule> result = scheduleService.page(page(page, size), query);
        return response(result, page, size, this::scheduleView);
    }

    @Override
    @Transactional
    public StaffScheduleView createSchedule(Long staffId, StaffScheduleRequest request, Long operatorId) {
        Staff staff = requireStaff(staffId);
        validateSchedule(staff, request, null);
        StaffSchedule schedule = new StaffSchedule();
        schedule.setStaffId(staffId);
        apply(schedule, request);
        scheduleService.save(schedule);
        audit(operatorId, "staff", "create-schedule", "POST", "/api/v1/admin/staff/" + staffId + "/schedules");
        return scheduleView(schedule);
    }

    @Override
    @Transactional
    public StaffScheduleView updateSchedule(Long staffId, Long scheduleId,
            StaffScheduleRequest request, Long operatorId) {
        Staff staff = requireStaff(staffId);
        validateSchedule(staff, request, scheduleId);
        StaffSchedule schedule = requireSchedule(staffId, scheduleId);
        apply(schedule, request);
        scheduleService.updateById(schedule);
        audit(operatorId, "staff", "update-schedule", "PUT",
                "/api/v1/admin/staff/" + staffId + "/schedules/" + scheduleId);
        return scheduleView(schedule);
    }

    @Override
    public PageResponse<ProductView> listProducts(int page, int size, String status) {
        LambdaQueryWrapper<Product> query = new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, 0)
                .eq(hasText(status), Product::getStatus, status)
                .orderByAsc(Product::getSort).orderByDesc(Product::getCreateTime);
        IPage<Product> result = productService.page(page(page, size), query);
        return response(result, page, size, this::productView);
    }

    @Override
    @Transactional
    public ProductView createProduct(ProductRequest request, Long operatorId) {
        requireProductCategory(request.categoryId());
        Product product = new Product();
        apply(product, request);
        product.setStock(0);
        product.setSalesCount(0);
        product.setStatus("ON_SALE");
        productService.save(product);
        audit(operatorId, "product", "create-item", "POST", "/api/v1/admin/products");
        return productView(product);
    }

    @Override
    @Transactional
    public ProductView updateProduct(Long id, ProductRequest request, Long operatorId) {
        requireProductCategory(request.categoryId());
        Product product = requireProduct(id);
        apply(product, request);
        productService.updateById(product);
        audit(operatorId, "product", "update-item", "PUT", "/api/v1/admin/products/" + id);
        return productView(product);
    }

    @Override
    @Transactional
    public ProductView disableProduct(Long id, Long operatorId) {
        Product product = requireProduct(id);
        product.setStatus("OFF_SALE");
        productService.updateById(product);
        audit(operatorId, "product", "disable-item", "POST", "/api/v1/admin/products/" + id + "/disable");
        return productView(product);
    }

    @Override
    @Transactional
    public ProductView updateProductStock(Long id, Integer stock, Long operatorId) {
        Product product = requireProductForUpdate(id);
        product.setStock(stock);
        productService.updateById(product);
        audit(operatorId, "product", "update-stock", "PUT", "/api/v1/admin/products/" + id + "/stock");
        return productView(product);
    }

    @Override
    public PageResponse<OperationLogView> listOperationLogs(int page, int size, String module) {
        LambdaQueryWrapper<AdminOperationLog> query = new LambdaQueryWrapper<AdminOperationLog>()
                .eq(hasText(module), AdminOperationLog::getModule, module)
                .orderByDesc(AdminOperationLog::getCreateTime);
        IPage<AdminOperationLog> result = operationLogService.page(page(page, size), query);
        return response(result, page, size, this::operationLogView);
    }

    private void audit(Long operatorId, String module, String operation, String method, String url) {
        AdminOperationLog entry = new AdminOperationLog();
        entry.setAdminId(operatorId);
        entry.setModule(module);
        entry.setOperation(operation);
        entry.setRequestMethod(method);
        entry.setRequestUrl(url);
        entry.setResult("SUCCESS");
        entry.setCreateTime(LocalDateTime.now());
        operationLogService.save(entry);
    }

    private Store requireStore(Long id) {
        Store store = storeService.getById(id);
        if (store == null || Integer.valueOf(1).equals(store.getDeleted())) {
            throw notFound("门店不存在");
        }
        return store;
    }

    private StoreConfig requireStoreConfig(Long storeId) {
        StoreConfig config = storeConfigService.getOne(
                new LambdaQueryWrapper<StoreConfig>().eq(StoreConfig::getStoreId, storeId), false);
        if (config == null) {
            throw notFound("门店配置不存在");
        }
        return config;
    }

    private ServiceItem requireServiceItem(Long id) {
        ServiceItem item = serviceItemService.getById(id);
        if (item == null || Integer.valueOf(1).equals(item.getDeleted())) {
            throw notFound("服务项目不存在");
        }
        return item;
    }

    private ServiceCategory requireServiceCategory(Long id) {
        ServiceCategory category = serviceCategoryService.getById(id);
        if (category == null || Integer.valueOf(1).equals(category.getDeleted())
                || !"ACTIVE".equals(category.getStatus())) {
            throw notFound("服务分类不存在或已禁用");
        }
        return category;
    }

    private Staff requireStaff(Long id) {
        Staff staff = staffService.getById(id);
        if (staff == null || Integer.valueOf(1).equals(staff.getDeleted())) {
            throw notFound("员工不存在");
        }
        return staff;
    }

    private StaffSchedule requireSchedule(Long staffId, Long scheduleId) {
        StaffSchedule schedule = scheduleService.getById(scheduleId);
        if (schedule == null || !staffId.equals(schedule.getStaffId())
                || Integer.valueOf(1).equals(schedule.getDeleted())) {
            throw notFound("排班不存在");
        }
        return schedule;
    }

    private Product requireProduct(Long id) {
        Product product = productService.getById(id);
        if (product == null || Integer.valueOf(1).equals(product.getDeleted())) {
            throw notFound("商品不存在");
        }
        return product;
    }

    private Product requireProductForUpdate(Long id) {
        Product product = productService.getOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id)
                .eq(Product::getDeleted, 0)
                .last("FOR UPDATE"), false);
        if (product == null) {
            throw notFound("商品不存在");
        }
        return product;
    }

    private ProductCategory requireProductCategory(Long id) {
        ProductCategory category = productCategoryService.getById(id);
        if (category == null || Integer.valueOf(1).equals(category.getDeleted())
                || !"ACTIVE".equals(category.getStatus())) {
            throw notFound("商品分类不存在或已禁用");
        }
        return category;
    }

    private void validateSchedule(Staff staff, StaffScheduleRequest request, Long excludedScheduleId) {
        if (!staff.getStoreId().equals(request.storeId())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "排班门店必须与员工所属门店一致");
        }
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "排班开始时间必须早于结束时间");
        }
        LambdaQueryWrapper<StaffSchedule> conflictQuery = new LambdaQueryWrapper<StaffSchedule>()
                .eq(StaffSchedule::getStaffId, staff.getId())
                .eq(StaffSchedule::getWorkDate, request.workDate())
                .eq(StaffSchedule::getDeleted, 0)
                .lt(StaffSchedule::getStartTime, request.endTime())
                .gt(StaffSchedule::getEndTime, request.startTime())
                .ne(excludedScheduleId != null, StaffSchedule::getId, excludedScheduleId);
        if (scheduleService.count(conflictQuery) > 0) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "该员工在所选时间段已有排班");
        }
    }

    private BusinessException notFound(String message) {
        return new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    private <T> Page<T> page(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return new Page<>(safePage, safeSize);
    }

    private <E, V> PageResponse<V> response(IPage<E> source, int page, int size,
            java.util.function.Function<E, V> mapper) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageResponse.of(source.getRecords().stream().map(mapper).toList(),
                source.getTotal(), safePage, safeSize);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private int flag(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private void apply(ServiceItem item, ServiceItemRequest request) {
        item.setCategoryId(request.categoryId());
        item.setName(request.name());
        item.setServiceMode(request.serviceMode());
        item.setPrice(request.price());
        item.setDurationMinutes(request.durationMinutes());
        item.setPetType(request.petType());
        item.setPetSize(request.petSize());
        item.setNeedAddress(flag(request.needAddress()));
        item.setNeedPet(flag(request.needPet()));
        item.setDescription(request.description());
        item.setCoverUrl(request.coverUrl());
        item.setSort(request.sort() == null ? 0 : request.sort());
    }

    private void apply(Staff staff, StaffRequest request) {
        staff.setStoreId(request.storeId());
        staff.setName(request.name());
        staff.setPhone(request.phone());
        staff.setAvatarUrl(request.avatarUrl());
        staff.setRole(request.role());
        staff.setDescription(request.description());
    }

    private void apply(StaffSchedule schedule, StaffScheduleRequest request) {
        schedule.setStoreId(request.storeId());
        schedule.setWorkDate(request.workDate());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setStatus(request.status());
        schedule.setRemark(request.remark());
    }

    private void apply(Product product, ProductRequest request) {
        product.setCategoryId(request.categoryId());
        product.setName(request.name());
        product.setCoverUrl(request.coverUrl());
        product.setPrice(request.price());
        product.setDescription(request.description());
        product.setPickupOnly(flag(request.pickupOnly()));
        product.setSort(request.sort() == null ? 0 : request.sort());
    }

    private StoreView storeView(Store store) {
        return new StoreView(store.getId(), store.getStoreName(), store.getPhone(), store.getAddress(),
                store.getLongitude(), store.getLatitude(), store.getBusinessHours(), store.getStatus(),
                store.getDescription());
    }

    private StoreConfigView storeConfigView(StoreConfig config) {
        return new StoreConfigView(config.getId(), config.getStoreId(), config.getHomeServiceRadiusKm(),
                config.getBookingAdvanceDays(), config.getBookingCancelHours(), config.getTimeSlotMinutes(),
                config.getAutoConfirmBooking() == 1, config.getContentAutoPublish() == 1);
    }

    private ServiceItemView serviceItemView(ServiceItem item) {
        return new ServiceItemView(item.getId(), item.getCategoryId(), item.getName(), item.getServiceMode(),
                item.getPrice(), item.getDurationMinutes(), item.getPetType(), item.getPetSize(),
                Integer.valueOf(1).equals(item.getNeedAddress()), Integer.valueOf(1).equals(item.getNeedPet()),
                item.getDescription(), item.getCoverUrl(), item.getStatus(), item.getSort());
    }

    private StaffView staffView(Staff staff) {
        return new StaffView(staff.getId(), staff.getStoreId(), staff.getName(), staff.getPhone(),
                staff.getAvatarUrl(), staff.getRole(), staff.getStatus(), staff.getDescription());
    }

    private StaffScheduleView scheduleView(StaffSchedule schedule) {
        return new StaffScheduleView(schedule.getId(), schedule.getStaffId(), schedule.getStoreId(),
                schedule.getWorkDate(), schedule.getStartTime(), schedule.getEndTime(),
                schedule.getStatus(), schedule.getRemark());
    }

    private ProductView productView(Product product) {
        return new ProductView(product.getId(), product.getCategoryId(), product.getName(), product.getCoverUrl(),
                product.getPrice(), product.getStock(), product.getSalesCount(), product.getDescription(),
                Integer.valueOf(1).equals(product.getPickupOnly()), product.getStatus(), product.getSort());
    }

    private OperationLogView operationLogView(AdminOperationLog entry) {
        return new OperationLogView(entry.getId(), entry.getAdminId(), entry.getModule(), entry.getOperation(),
                entry.getRequestMethod(), entry.getRequestUrl(), entry.getResult(),
                entry.getErrorMessage(), entry.getCreateTime());
    }
}
