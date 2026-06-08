package com.petcare.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petcare.service.entity.ServiceCategory;

import java.util.List;

public interface ServiceCategoryService extends IService<ServiceCategory> {

    /**
     * Lists all active (status=ACTIVE, deleted=0) categories, sorted by sort ASC.
     */
    List<ServiceCategory> listActiveCategories();
}
