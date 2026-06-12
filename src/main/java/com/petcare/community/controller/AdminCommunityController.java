package com.petcare.community.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.pagination.PageResponse;
import com.petcare.community.dto.AdminReportHandleRequest;
import com.petcare.community.dto.AdminReviewRequest;
import com.petcare.community.dto.CommentResponse;
import com.petcare.community.dto.PostReportResponse;
import com.petcare.community.dto.PostResponse;
import com.petcare.community.service.CommunityAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin community moderation endpoints.
 * All endpoints require authentication and specific permission codes.
 */
@RestController
@RequestMapping("/api/v1/admin/community")
public class AdminCommunityController {

    private final CommunityAdminService adminService;

    public AdminCommunityController(CommunityAdminService adminService) {
        this.adminService = adminService;
    }

    // ==================== Post Management ====================

    @GetMapping("/posts")
    @PreAuthorize("hasAuthority('community:post:read')")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> listPosts(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<PostResponse> result = adminService.listPosts(status, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/posts/{id}")
    @PreAuthorize("hasAuthority('community:post:read')")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long id) {
        PostResponse result = adminService.getPost(id);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/posts/{id}/approve")
    @PreAuthorize("hasAuthority('community:post:approve')")
    public ResponseEntity<ApiResponse<PostResponse>> approvePost(
            @PathVariable Long id,
            @RequestBody(required = false) AdminReviewRequest request) {
        Long adminId = resolveAdminId();
        String remark = request != null ? request.remark() : null;
        PostResponse result = adminService.approvePost(adminId, id, remark);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/posts/{id}/reject")
    @PreAuthorize("hasAuthority('community:post:reject')")
    public ResponseEntity<ApiResponse<PostResponse>> rejectPost(
            @PathVariable Long id,
            @RequestBody(required = false) AdminReviewRequest request) {
        Long adminId = resolveAdminId();
        String remark = request != null ? request.remark() : null;
        PostResponse result = adminService.rejectPost(adminId, id, remark);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/posts/{id}/hide")
    @PreAuthorize("hasAuthority('community:post:hide')")
    public ResponseEntity<ApiResponse<PostResponse>> hidePost(@PathVariable Long id) {
        Long adminId = resolveAdminId();
        PostResponse result = adminService.hidePost(adminId, id);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/posts/{id}/delete")
    @PreAuthorize("hasAuthority('community:post:delete')")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        Long adminId = resolveAdminId();
        adminService.deletePost(adminId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ==================== Comment Management ====================

    @GetMapping("/comments")
    @PreAuthorize("hasAuthority('community:post:read')")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> listComments(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CommentResponse> result = adminService.listComments(status, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/comments/{id}/approve")
    @PreAuthorize("hasAuthority('community:post:approve')")
    public ResponseEntity<ApiResponse<CommentResponse>> approveComment(
            @PathVariable Long id,
            @RequestBody(required = false) AdminReviewRequest request) {
        Long adminId = resolveAdminId();
        String remark = request != null ? request.remark() : null;
        CommentResponse result = adminService.approveComment(adminId, id, remark);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/comments/{id}/reject")
    @PreAuthorize("hasAuthority('community:post:reject')")
    public ResponseEntity<ApiResponse<CommentResponse>> rejectComment(
            @PathVariable Long id,
            @RequestBody(required = false) AdminReviewRequest request) {
        Long adminId = resolveAdminId();
        String remark = request != null ? request.remark() : null;
        CommentResponse result = adminService.rejectComment(adminId, id, remark);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/comments/{id}/hide")
    @PreAuthorize("hasAuthority('community:comment:hide')")
    public ResponseEntity<ApiResponse<CommentResponse>> hideComment(@PathVariable Long id) {
        Long adminId = resolveAdminId();
        CommentResponse result = adminService.hideComment(adminId, id);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/comments/{id}/delete")
    @PreAuthorize("hasAuthority('community:comment:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        Long adminId = resolveAdminId();
        adminService.deleteComment(adminId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ==================== Report Management ====================

    @GetMapping("/reports")
    @PreAuthorize("hasAuthority('community:report:handle')")
    public ResponseEntity<ApiResponse<PageResponse<PostReportResponse>>> listReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<PostReportResponse> result = adminService.listReports(status, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/reports/{id}/handle")
    @PreAuthorize("hasAuthority('community:report:handle')")
    public ResponseEntity<ApiResponse<Void>> handleReport(
            @PathVariable Long id,
            @Valid @RequestBody AdminReportHandleRequest request) {
        Long adminId = resolveAdminId();
        adminService.handleReport(adminId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ==================== Private Helpers ====================

    private Long resolveAdminId() {
        return com.petcare.common.security.SecurityContextHelper.getCurrentAdminId()
                .orElseThrow(() -> new com.petcare.common.exception.BusinessException(
                        com.petcare.common.exception.ErrorCode.UNAUTHORIZED, "管理员未认证"));
    }
}
