package com.petcare.community.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.pagination.PageResponse;
import com.petcare.community.dto.CommentCreateRequest;
import com.petcare.community.dto.CommentResponse;
import com.petcare.community.dto.PostCreateRequest;
import com.petcare.community.dto.PostResponse;
import com.petcare.community.dto.PublicCommentResponse;
import com.petcare.community.dto.PublicPostDetailResponse;
import com.petcare.community.dto.PublicPostSummaryResponse;
import com.petcare.community.dto.ReportPostRequest;
import com.petcare.community.service.CommunityInteractionService;
import com.petcare.community.service.CommunityPostApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User-facing post and comment endpoints.
 *
 * Note: User JWT is not yet implemented. User mutation endpoints will return 401
 * until user authentication is added.
 */
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final CommunityPostApplicationService postService;
    private final CommunityInteractionService interactionService;

    public PostController(CommunityPostApplicationService postService,
                          CommunityInteractionService interactionService) {
        this.postService = postService;
        this.interactionService = interactionService;
    }

    /**
     * List published posts with optional topic filter and pagination.
     * Returns the public summary view (no private identifiers or internal status).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PublicPostSummaryResponse>>> listPosts(
            @RequestParam(required = false) Long topicId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<PublicPostSummaryResponse> result = postService.listPublicPosts(topicId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Get published post detail. Only PUBLISHED posts are visible; everything else
     * resolves to a safe 404 so existence and audit status are never leaked.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PublicPostDetailResponse>> getPostDetail(@PathVariable Long id) {
        PublicPostDetailResponse result = postService.getPublicPostDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Create a new post. User identity comes from security context, not request body.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostCreateRequest request) {
        Long currentUserId = resolveCurrentUserId();
        PostResponse result = postService.createPost(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }

    /**
     * List published comments for a published post. The parent post must be
     * PUBLISHED; otherwise a safe 404 is returned. Only PUBLISHED comments are
     * returned, without private identifiers or internal status.
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<PublicCommentResponse>>> listComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<PublicCommentResponse> result = postService.listPublicComments(postId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Create a comment on a post. User identity from security context.
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request) {
        Long currentUserId = resolveCurrentUserId();
        CommentResponse result = postService.createComment(currentUserId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }

    /**
     * Like a post. Idempotent.
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> likePost(@PathVariable Long postId) {
        Long currentUserId = resolveCurrentUserId();
        interactionService.likePost(currentUserId, postId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Remove like from a post. Idempotent.
     */
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> unlikePost(@PathVariable Long postId) {
        Long currentUserId = resolveCurrentUserId();
        interactionService.unlikePost(currentUserId, postId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Favorite a post. Idempotent.
     */
    @PostMapping("/{postId}/favorite")
    public ResponseEntity<ApiResponse<Void>> favoritePost(@PathVariable Long postId) {
        Long currentUserId = resolveCurrentUserId();
        interactionService.favoritePost(currentUserId, postId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Remove favorite from a post. Idempotent.
     */
    @DeleteMapping("/{postId}/favorite")
    public ResponseEntity<ApiResponse<Void>> unfavoritePost(@PathVariable Long postId) {
        Long currentUserId = resolveCurrentUserId();
        interactionService.unfavoritePost(currentUserId, postId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * Report a post.
     */
    @PostMapping("/{postId}/reports")
    public ResponseEntity<ApiResponse<Void>> reportPost(
            @PathVariable Long postId,
            @Valid @RequestBody ReportPostRequest request) {
        Long currentUserId = resolveCurrentUserId();
        interactionService.reportPost(currentUserId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null));
    }

    /**
     * Resolves current user ID from security context.
     * Returns 401 if no user identity available.
     */
    private Long resolveCurrentUserId() {
        throw new BusinessException(ErrorCode.UNAUTHORIZED,
                "用户端功能暂未开放，请等待用户登录功能上线");
    }
}
