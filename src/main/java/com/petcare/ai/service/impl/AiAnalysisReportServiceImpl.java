package com.petcare.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.ai.entity.AiAnalysisReport;
import com.petcare.ai.mapper.AiAnalysisReportMapper;
import com.petcare.ai.service.AiAnalysisReportService;
import org.springframework.stereotype.Service;

@Service
public class AiAnalysisReportServiceImpl extends ServiceImpl<AiAnalysisReportMapper, AiAnalysisReport> implements AiAnalysisReportService {
}
