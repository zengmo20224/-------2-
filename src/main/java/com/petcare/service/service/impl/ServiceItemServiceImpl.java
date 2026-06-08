package com.petcare.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.service.entity.ServiceItem;
import com.petcare.service.mapper.ServiceItemMapper;
import com.petcare.service.service.ServiceItemService;
import org.springframework.stereotype.Service;

@Service
public class ServiceItemServiceImpl extends ServiceImpl<ServiceItemMapper, ServiceItem> implements ServiceItemService {

    @Override
    public IPage<ServiceItem> listOnSaleItems(Long categoryId, String serviceMode,
                                              String petType, String petSize,
                                              Page<ServiceItem> page) {
        LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getStatus, "ON_SALE")
                .orderByAsc(ServiceItem::getSort);

        if (categoryId != null) {
            wrapper.eq(ServiceItem::getCategoryId, categoryId);
        }
        if (serviceMode != null && !serviceMode.isBlank()) {
            wrapper.and(w -> w.eq(ServiceItem::getServiceMode, serviceMode)
                    .or().eq(ServiceItem::getServiceMode, "BOTH"));
        }
        if (petType != null && !petType.isBlank()) {
            wrapper.and(w -> w.eq(ServiceItem::getPetType, petType)
                    .or().eq(ServiceItem::getPetType, "ALL"));
        }
        if (petSize != null && !petSize.isBlank()) {
            wrapper.and(w -> w.eq(ServiceItem::getPetSize, petSize)
                    .or().eq(ServiceItem::getPetSize, "ALL"));
        }

        return page(page, wrapper);
    }

    @Override
    public ServiceItem getOnSaleItem(Long id) {
        ServiceItem item = getById(id);
        if (item == null || !"ON_SALE".equals(item.getStatus())) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "服务项目不存在或已下架");
        }
        return item;
    }
}
