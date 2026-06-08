package com.petcare.moderation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.moderation.entity.SensitiveWord;
import com.petcare.moderation.mapper.SensitiveWordMapper;
import com.petcare.moderation.service.SensitiveWordService;
import org.springframework.stereotype.Service;

@Service
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord> implements SensitiveWordService {
}
