package com.petcare.ai.controller;

import com.petcare.ai.dto.AiUsageResponse;
import com.petcare.ai.service.AiUsageApplicationService;
import com.petcare.common.api.ApiResponse;
import com.petcare.common.pagination.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin AI usage query endpoints.
 * Does not expose full prompts, responses, or provider raw errors.
 */
@RestController
@RequestMapping("/api/v1/admin/ai/usage")
public class AdminAiUsageController {

    private final AiUsageApplicationService usageService;

    public AdminAiUsageController(AiUsageApplicationService usageService) {
        this.usageService = usageService;
    }

    /**
     * List AI usage logs with optional filters.
     * Requires ai:usage:read permission.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ai:usage:read')")
    public ResponseEntity<ApiResponse<PageResponse<AiUsageResponse>>> listUsage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String apiType,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        PageResponse<AiUsageResponse> response = usageService.listUsage(page, size, apiType, success, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
