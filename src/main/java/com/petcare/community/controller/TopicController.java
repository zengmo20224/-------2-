package com.petcare.community.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.community.dto.TopicResponse;
import com.petcare.community.service.CommunityPostApplicationService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User-facing topic endpoints.
 * Topics are read-only for regular users.
 */
@RestController
@RequestMapping("/api/v1/topics")
public class TopicController {

    private final CommunityPostApplicationService postService;

    public TopicController(CommunityPostApplicationService postService) {
        this.postService = postService;
    }

    /**
     * List all active topics.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TopicResponse>>> listTopics() {
        List<TopicResponse> topics = postService.listTopics();
        return ResponseEntity.ok(ApiResponse.ok(topics));
    }

    /**
     * Get a single topic by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TopicResponse>> getTopic(@PathVariable Long id) {
        TopicResponse topic = postService.getTopic(id);
        return ResponseEntity.ok(ApiResponse.ok(topic));
    }
}
