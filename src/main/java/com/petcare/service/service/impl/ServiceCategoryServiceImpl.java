package com.petcare.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.service.entity.ServiceCategory;
import com.petcare.service.mapper.ServiceCategoryMapper;
import com.petcare.service.service.ServiceCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCategoryServiceImpl extends ServiceImpl<ServiceCategoryMapper, ServiceCategory> implements ServiceCategoryService {

    @Override
    public List<ServiceCategory> listActiveCategories() {
        return list(new LambdaQueryWrapper<ServiceCategory>()
                .eq(ServiceCategory::getStatus, "ACTIVE")
                .orderByAsc(ServiceCategory::getSort));
    }
}
