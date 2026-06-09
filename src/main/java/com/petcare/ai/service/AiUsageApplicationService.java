package com.petcare.ai.service;

import com.petcare.ai.dto.AiUsageResponse;
import com.petcare.common.pagination.PageResponse;

/**
 * Application service for querying AI usage logs.
 * Does not expose full prompts, responses, or provider raw errors.
 */
public interface AiUsageApplicationService {

    /**
     * Lists AI usage logs with optional filters.
     * Requires ai:usage:read permission.
     *
     * @param page      page number (1-based)
     * @param size      page size
     * @param apiType   optional API type filter
     * @param success   optional success status filter
     * @param startDate optional start date filter
     * @param endDate   optional end date filter
     * @return paginated usage log entries
     */
    PageResponse<AiUsageResponse> listUsage(int page, int size, String apiType,
                                             Boolean success, String startDate, String endDate);
}
