package com.petcare.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.ai.entity.FaqKnowledge;
import com.petcare.ai.mapper.FaqKnowledgeMapper;
import com.petcare.ai.service.FaqKnowledgeService;
import org.springframework.stereotype.Service;

@Service
public class FaqKnowledgeServiceImpl extends ServiceImpl<FaqKnowledgeMapper, FaqKnowledge> implements FaqKnowledgeService {
}
