package com.petcare.community.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.pagination.PageResponse;
import com.petcare.common.security.SecurityContextHelper;
import com.petcare.community.dto.PublicPostSummaryResponse;
import com.petcare.community.service.CommunityPostApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User-personal community endpoints.
 * All endpoints require an authenticated user.
 */
@RestController
@RequestMapping("/api/v1/user/community")
public class UserCommunityController {

    private final CommunityPostApplicationService postService;

    public UserCommunityController(CommunityPostApplicationService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PageResponse<PublicPostSummaryResponse>>> myPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = resolveUserId();
        PageResponse<PublicPostSummaryResponse> result = postService.listMyPosts(userId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/liked-posts")
    public ResponseEntity<ApiResponse<PageResponse<PublicPostSummaryResponse>>> myLikedPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = resolveUserId();
        PageResponse<PublicPostSummaryResponse> result = postService.listMyLikedPosts(userId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/favorited-posts")
    public ResponseEntity<ApiResponse<PageResponse<PublicPostSummaryResponse>>> myFavoritedPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = resolveUserId();
        PageResponse<PublicPostSummaryResponse> result = postService.listMyFavoritedPosts(userId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    private Long resolveUserId() {
        return SecurityContextHelper.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"));
    }
}
