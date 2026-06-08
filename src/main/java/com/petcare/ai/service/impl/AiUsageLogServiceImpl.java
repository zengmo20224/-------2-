package com.petcare.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.ai.entity.AiUsageLog;
import com.petcare.ai.mapper.AiUsageLogMapper;
import com.petcare.ai.service.AiUsageLogService;
import org.springframework.stereotype.Service;

@Service
public class AiUsageLogServiceImpl extends ServiceImpl<AiUsageLogMapper, AiUsageLog> implements AiUsageLogService {
}
