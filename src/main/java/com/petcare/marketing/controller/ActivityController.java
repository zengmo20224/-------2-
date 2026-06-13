package com.petcare.marketing.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.pagination.PageResponse;
import com.petcare.marketing.dto.MarketingActivityDtos;
import com.petcare.marketing.service.MarketingApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public-facing marketing activity endpoints.
 * Only ACTIVE activities are visible to anonymous readers.
 */
@RestController
@RequestMapping("/api/v1/activities")
public class ActivityController {

    private final MarketingApplicationService applicationService;

    public ActivityController(MarketingApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MarketingActivityDtos.PublicActivitySummary>>> listActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<MarketingActivityDtos.PublicActivitySummary> result =
                applicationService.listPublicActivities(page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MarketingActivityDtos.PublicActivitySummary>> getActivity(
            @PathVariable Long id) {
        MarketingActivityDtos.PublicActivitySummary result =
                applicationService.getPublicActivity(id);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
