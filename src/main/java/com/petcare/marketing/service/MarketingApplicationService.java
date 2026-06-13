package com.petcare.marketing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.pagination.PageResponse;
import com.petcare.marketing.dto.MarketingActivityDtos;
import com.petcare.marketing.entity.ActivityProduct;
import com.petcare.marketing.entity.ActivityService;
import com.petcare.marketing.entity.MarketingActivity;
import com.petcare.marketing.enums.ActivityStatus;
import com.petcare.product.entity.Product;
import com.petcare.product.service.ProductService;
import com.petcare.service.entity.ServiceItem;
import com.petcare.service.service.ServiceItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Application service for marketing activity operations.
 * Handles public reads, admin CRUD and product/service associations.
 */
@Service
public class MarketingApplicationService {

    private final MarketingActivityService activityService;
    private final ActivityProductService activityProductService;
    private final ActivityServiceService activityServiceLinkService;
    private final ProductService productService;
    private final ServiceItemService serviceItemService;

    public MarketingApplicationService(MarketingActivityService activityService,
                                        ActivityProductService activityProductService,
                                        ActivityServiceService activityServiceLinkService,
                                        ProductService productService,
                                        ServiceItemService serviceItemService) {
        this.activityService = activityService;
        this.activityProductService = activityProductService;
        this.activityServiceLinkService = activityServiceLinkService;
        this.productService = productService;
        this.serviceItemService = serviceItemService;
    }

    // ---- Public reads ----

    public PageResponse<MarketingActivityDtos.PublicActivitySummary> listPublicActivities(int page, int size) {
        Page<MarketingActivity> pageParam = new Page<>(page, Math.min(size, 50));
        IPage<MarketingActivity> result = activityService.lambdaQuery()
                .eq(MarketingActivity::getStatus, ActivityStatus.ACTIVE.getCode())
                .orderByDesc(MarketingActivity::getCreateTime)
                .page(pageParam);

        List<MarketingActivityDtos.PublicActivitySummary> items = result.getRecords().stream()
                .map(this::toPublicSummary)
                .toList();

        return PageResponse.of(items, result.getTotal(), page, size);
    }

    public MarketingActivityDtos.PublicActivitySummary getPublicActivity(Long id) {
        MarketingActivity activity = activityService.getById(id);
        if (activity == null || !ActivityStatus.ACTIVE.getCode().equals(activity.getStatus())) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "活动不存在或未上线");
        }
        return toPublicSummary(activity);
    }

    // ---- Admin CRUD ----

    public PageResponse<MarketingActivityDtos.AdminActivitySummary> listAdminActivities(int page, int size) {
        Page<MarketingActivity> pageParam = new Page<>(page, Math.min(size, 100));
        IPage<MarketingActivity> result = activityService.lambdaQuery()
                .orderByDesc(MarketingActivity::getCreateTime)
                .page(pageParam);

        List<MarketingActivityDtos.AdminActivitySummary> items = result.getRecords().stream()
                .map(this::toAdminSummary)
                .toList();

        return PageResponse.of(items, result.getTotal(), page, size);
    }

    public MarketingActivityDtos.AdminActivityDetail getAdminActivityDetail(Long id) {
        MarketingActivity activity = activityService.getById(id);
        if (activity == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "活动不存在");
        }

        List<ActivityProduct> productLinks = activityProductService.lambdaQuery()
                .eq(ActivityProduct::getActivityId, id)
                .list();
        List<ActivityService> serviceLinks = activityServiceLinkService.lambdaQuery()
                .eq(ActivityService::getActivityId, id)
                .list();

        List<Long> productIds = productLinks.stream().map(ActivityProduct::getProductId).toList();
        List<Long> serviceItemIds = serviceLinks.stream().map(ActivityService::getServiceItemId).toList();

        return new MarketingActivityDtos.AdminActivityDetail(
                activity.getId(), activity.getTitle(), activity.getActivityType(),
                activity.getDescription(), activity.getStartTime(), activity.getEndTime(),
                activity.getStatus(), productIds, serviceItemIds);
    }

    @Transactional
    public MarketingActivityDtos.AdminActivityDetail createActivity(MarketingActivityDtos.ActivityUpsertRequest request) {
        MarketingActivity activity = new MarketingActivity();
        activity.setTitle(request.title());
        activity.setActivityType(request.activityType());
        activity.setDescription(request.description());
        activity.setStartTime(request.startTime());
        activity.setEndTime(request.endTime());
        activity.setStatus(ActivityStatus.DRAFT.getCode());
        activityService.save(activity);

        replaceAssociations(activity.getId(), request.productIds(), request.serviceItemIds());

        return getAdminActivityDetail(activity.getId());
    }

    @Transactional
    public MarketingActivityDtos.AdminActivityDetail updateActivity(Long id, MarketingActivityDtos.ActivityUpsertRequest request) {
        MarketingActivity activity = activityService.getById(id);
        if (activity == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "活动不存在");
        }

        activity.setTitle(request.title());
        activity.setActivityType(request.activityType());
        activity.setDescription(request.description());
        activity.setStartTime(request.startTime());
        activity.setEndTime(request.endTime());
        activityService.updateById(activity);

        replaceAssociations(id, request.productIds(), request.serviceItemIds());

        return getAdminActivityDetail(id);
    }

    @Transactional
    public void updateActivityStatus(Long id, String status) {
        MarketingActivity activity = activityService.getById(id);
        if (activity == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "活动不存在");
        }

        // Validate status transition
        Set<String> validStatuses = Set.of(
                ActivityStatus.DRAFT.getCode(),
                ActivityStatus.ACTIVE.getCode(),
                ActivityStatus.ENDED.getCode(),
                ActivityStatus.CANCELLED.getCode()
        );
        if (!validStatuses.contains(status)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "无效的活动状态: " + status);
        }

        activity.setStatus(status);
        activityService.updateById(activity);
    }

    @Transactional
    public void deleteActivity(Long id) {
        MarketingActivity activity = activityService.getById(id);
        if (activity == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "活动不存在");
        }

        activityService.removeById(id);
        activityProductService.lambdaQuery()
                .eq(ActivityProduct::getActivityId, id)
                .list()
                .forEach(link -> activityProductService.removeById(link.getId()));
        activityServiceLinkService.lambdaQuery()
                .eq(ActivityService::getActivityId, id)
                .list()
                .forEach(link -> activityServiceLinkService.removeById(link.getId()));
    }

    // ---- Helpers ----

    private void replaceAssociations(Long activityId, List<Long> productIds, List<Long> serviceItemIds) {
        // Clear existing
        activityProductService.lambdaUpdate()
                .eq(ActivityProduct::getActivityId, activityId)
                .remove();
        activityServiceLinkService.lambdaUpdate()
                .eq(ActivityService::getActivityId, activityId)
                .remove();

        // Insert new product links
        if (productIds != null) {
            for (Long productId : productIds) {
                ActivityProduct link = new ActivityProduct();
                link.setActivityId(activityId);
                link.setProductId(productId);
                activityProductService.save(link);
            }
        }

        // Insert new service links
        if (serviceItemIds != null) {
            for (Long serviceItemId : serviceItemIds) {
                ActivityService link = new ActivityService();
                link.setActivityId(activityId);
                link.setServiceItemId(serviceItemId);
                activityServiceLinkService.save(link);
            }
        }
    }

    private MarketingActivityDtos.PublicActivitySummary toPublicSummary(MarketingActivity activity) {
        List<String> productNames = getProductNames(activity.getId());
        List<String> serviceNames = getServiceNames(activity.getId());

        return new MarketingActivityDtos.PublicActivitySummary(
                activity.getId(), activity.getTitle(), activity.getActivityType(),
                activity.getDescription(), activity.getStartTime(), activity.getEndTime(),
                productNames, serviceNames);
    }

    private MarketingActivityDtos.AdminActivitySummary toAdminSummary(MarketingActivity activity) {
        return new MarketingActivityDtos.AdminActivitySummary(
                activity.getId(), activity.getTitle(), activity.getActivityType(),
                activity.getDescription(), activity.getStartTime(), activity.getEndTime(),
                activity.getStatus());
    }

    private List<String> getProductNames(Long activityId) {
        List<ActivityProduct> links = activityProductService.lambdaQuery()
                .eq(ActivityProduct::getActivityId, activityId)
                .list();
        if (links.isEmpty()) return Collections.emptyList();

        List<Long> productIds = links.stream().map(ActivityProduct::getProductId).toList();
        return productService.lambdaQuery()
                .in(Product::getId, productIds)
                .list()
                .stream()
                .map(Product::getName)
                .toList();
    }

    private List<String> getServiceNames(Long activityId) {
        List<ActivityService> links = activityServiceLinkService.lambdaQuery()
                .eq(ActivityService::getActivityId, activityId)
                .list();
        if (links.isEmpty()) return Collections.emptyList();

        List<Long> serviceItemIds = links.stream().map(ActivityService::getServiceItemId).toList();
        return serviceItemService.lambdaQuery()
                .in(ServiceItem::getId, serviceItemIds)
                .list()
                .stream()
                .map(ServiceItem::getName)
                .toList();
    }
}
