package com.petcare.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.service.entity.ServiceItemImage;
import com.petcare.service.mapper.ServiceItemImageMapper;
import com.petcare.service.service.ServiceItemImageService;
import org.springframework.stereotype.Service;

@Service
public class ServiceItemImageServiceImpl extends ServiceImpl<ServiceItemImageMapper, ServiceItemImage>
        implements ServiceItemImageService {
}
