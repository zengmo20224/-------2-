package com.petcare.marketing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.marketing.entity.ActivityService;
import com.petcare.marketing.mapper.ActivityServiceMapper;
import com.petcare.marketing.service.ActivityServiceService;
import org.springframework.stereotype.Service;

@Service
public class ActivityServiceServiceImpl extends ServiceImpl<ActivityServiceMapper, ActivityService> implements ActivityServiceService {
}
