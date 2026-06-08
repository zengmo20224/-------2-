package com.petcare.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.petcare.service.entity.ServiceItem;

public interface ServiceItemService extends IService<ServiceItem> {

    /**
     * Lists on-sale service items with optional filters, paginated.
     * Only returns items where status=ON_SALE and deleted=0.
     *
     * @param categoryId  optional filter by category
     * @param serviceMode optional filter by service mode (STORE/HOME/BOTH)
     * @param petType     optional filter by pet type
     * @param petSize     optional filter by pet size
     * @param page        pagination parameters
     * @return paginated results
     */
    IPage<ServiceItem> listOnSaleItems(Long categoryId, String serviceMode,
                                       String petType, String petSize,
                                       Page<ServiceItem> page);

    /**
     * Gets a single on-sale service item by ID.
     * Throws BusinessException if not found or not on sale.
     */
    ServiceItem getOnSaleItem(Long id);
}
