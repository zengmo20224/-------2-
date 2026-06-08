package com.petcare.moderation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.moderation.entity.ContentReviewRecord;
import com.petcare.moderation.mapper.ContentReviewRecordMapper;
import com.petcare.moderation.service.ContentReviewRecordService;
import org.springframework.stereotype.Service;

@Service
public class ContentReviewRecordServiceImpl extends ServiceImpl<ContentReviewRecordMapper, ContentReviewRecord> implements ContentReviewRecordService {
}
