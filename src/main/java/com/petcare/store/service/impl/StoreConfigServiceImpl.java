package com.petcare.store.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.store.entity.StoreConfig;
import com.petcare.store.mapper.StoreConfigMapper;
import com.petcare.store.service.StoreConfigService;
import org.springframework.stereotype.Service;

@Service
public class StoreConfigServiceImpl extends ServiceImpl<StoreConfigMapper, StoreConfig> implements StoreConfigService {
}
