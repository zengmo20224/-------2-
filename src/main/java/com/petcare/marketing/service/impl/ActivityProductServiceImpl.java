package com.petcare.marketing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.marketing.entity.ActivityProduct;
import com.petcare.marketing.mapper.ActivityProductMapper;
import com.petcare.marketing.service.ActivityProductService;
import org.springframework.stereotype.Service;

@Service
public class ActivityProductServiceImpl extends ServiceImpl<ActivityProductMapper, ActivityProduct> implements ActivityProductService {
}
