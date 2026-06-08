package com.petcare.marketing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.marketing.entity.MarketingActivity;
import com.petcare.marketing.mapper.MarketingActivityMapper;
import com.petcare.marketing.service.MarketingActivityService;
import org.springframework.stereotype.Service;

@Service
public class MarketingActivityServiceImpl extends ServiceImpl<MarketingActivityMapper, MarketingActivity> implements MarketingActivityService {
}
