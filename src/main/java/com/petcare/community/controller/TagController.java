package com.petcare.community.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.community.dto.TagResponse;
import com.petcare.community.service.CommunityPostApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * User-facing tag endpoints.
 * Supports searching existing tags by keyword for autocomplete.
 */
@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final CommunityPostApplicationService postService;

    public TagController(CommunityPostApplicationService postService) {
        this.postService = postService;
    }

    /**
     * Search tags by keyword, ordered by usage count desc.
     * Returns top 20 matches for autocomplete.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> searchTags(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") int limit) {
        List<TagResponse> tags = postService.searchTags(keyword, limit);
        return ResponseEntity.ok(ApiResponse.ok(tags));
    }

    /**
     * List popular tags (highest usage count).
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<TagResponse>>> popularTags(
            @RequestParam(defaultValue = "20") int limit) {
        List<TagResponse> tags = postService.popularTags(limit);
        return ResponseEntity.ok(ApiResponse.ok(tags));
    }
}
