package com.petcare.marketing.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.pagination.PageResponse;
import com.petcare.marketing.dto.MarketingActivityDtos;
import com.petcare.marketing.service.MarketingApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin marketing activity management endpoints.
 * Requires admin authentication.
 */
@RestController
@RequestMapping("/api/v1/admin/activities")
public class AdminActivityController {

    private final MarketingApplicationService applicationService;

    public AdminActivityController(MarketingApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('marketing:activity:read')")
    public ResponseEntity<ApiResponse<PageResponse<MarketingActivityDtos.AdminActivitySummary>>> listActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        PageResponse<MarketingActivityDtos.AdminActivitySummary> result =
                applicationService.listAdminActivities(page, size, status);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('marketing:activity:read')")
    public ResponseEntity<ApiResponse<MarketingActivityDtos.AdminActivityDetail>> getActivity(
            @PathVariable Long id) {
        MarketingActivityDtos.AdminActivityDetail result =
                applicationService.getAdminActivityDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('marketing:activity:manage')")
    public ResponseEntity<ApiResponse<MarketingActivityDtos.AdminActivityDetail>> createActivity(
            @Valid @RequestBody MarketingActivityDtos.ActivityUpsertRequest request) {
        MarketingActivityDtos.AdminActivityDetail result =
                applicationService.createActivity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('marketing:activity:manage')")
    public ResponseEntity<ApiResponse<MarketingActivityDtos.AdminActivityDetail>> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody MarketingActivityDtos.ActivityUpsertRequest request) {
        MarketingActivityDtos.AdminActivityDetail result =
                applicationService.updateActivity(id, request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('marketing:activity:manage')")
    public ResponseEntity<ApiResponse<Void>> updateActivityStatus(
            @PathVariable Long id,
            @Valid @RequestBody MarketingActivityDtos.ActivityStatusRequest request) {
        applicationService.updateActivityStatus(id, request.status());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('marketing:activity:manage')")
    public ResponseEntity<ApiResponse<Void>> deleteActivity(@PathVariable Long id) {
        applicationService.deleteActivity(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
