package com.petcare.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcare.ai.dto.AiUsageResponse;
import com.petcare.ai.entity.AiUsageLog;
import com.petcare.ai.mapper.AiUsageLogMapper;
import com.petcare.ai.service.AiUsageApplicationService;
import com.petcare.common.pagination.PageResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of AI usage query service.
 * Does not expose full prompts, responses, or provider raw errors.
 */
@Service
public class AiUsageApplicationServiceImpl implements AiUsageApplicationService {

    private final AiUsageLogMapper usageLogMapper;

    public AiUsageApplicationServiceImpl(AiUsageLogMapper usageLogMapper) {
        this.usageLogMapper = usageLogMapper;
    }

    @Override
    public PageResponse<AiUsageResponse> listUsage(int page, int size, String apiType,
                                                     Boolean success, String startDate, String endDate) {
        Page<AiUsageLog> pageParam = new Page<>(page, size);
        QueryWrapper<AiUsageLog> query = new QueryWrapper<AiUsageLog>()
                .orderByDesc("create_time");

        if (apiType != null && !apiType.isBlank()) {
            query.eq("api_type", apiType);
        }
        if (success != null) {
            query.eq("success", success ? 1 : 0);
        }
        if (startDate != null && !startDate.isBlank()) {
            query.ge("create_time", LocalDate.parse(startDate).atStartOfDay());
        }
        if (endDate != null && !endDate.isBlank()) {
            query.le("create_time", LocalDate.parse(endDate).plusDays(1).atStartOfDay());
        }

        Page<AiUsageLog> result = usageLogMapper.selectPage(pageParam, query);

        List<AiUsageResponse> items = result.getRecords().stream()
                .map(this::toUsageResponse)
                .toList();

        return PageResponse.of(items, result.getTotal(), page, size);
    }

    private AiUsageResponse toUsageResponse(AiUsageLog u) {
        return new AiUsageResponse(
                u.getId(),
                u.getUserId(),
                u.getAdminId(),
                u.getApiType(),
                u.getModelName(),
                u.getPromptTokens(),
                u.getCompletionTokens(),
                u.getTotalTokens(),
                u.getSuccess() != null && u.getSuccess() == 1,
                u.getErrorMessage(),
                u.getCreateTime()
        );
    }
}
