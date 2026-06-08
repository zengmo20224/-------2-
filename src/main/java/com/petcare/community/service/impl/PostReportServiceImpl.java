package com.petcare.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.community.entity.PostReport;
import com.petcare.community.mapper.PostReportMapper;
import com.petcare.community.service.PostReportService;
import org.springframework.stereotype.Service;

@Service
public class PostReportServiceImpl extends ServiceImpl<PostReportMapper, PostReport> implements PostReportService {
}
