package com.petcare.ai.controller;

import com.petcare.ai.dto.PostAssistantRequest;
import com.petcare.ai.dto.PostAssistantResponse;
import com.petcare.ai.service.AiPostAssistantService;
import com.petcare.common.api.ApiResponse;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User-facing AI post assistant endpoint.
 * Generates suggested post drafts — never auto-publishes.
 */
@RestController
@RequestMapping("/api/v1/ai/post-assistant")
public class AiPostAssistantController {

    private final AiPostAssistantService postAssistantService;

    public AiPostAssistantController(AiPostAssistantService postAssistantService) {
        this.postAssistantService = postAssistantService;
    }

    /**
     * Generate a suggested post draft based on user-provided facts.
     * The generated content is a draft and must be confirmed by the user.
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<PostAssistantResponse>> generateDraft(
            @Valid @RequestBody PostAssistantRequest request) {
        Long currentUserId = resolveCurrentUserId();
        PostAssistantResponse response = postAssistantService.generateDraft(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Resolves current user ID from the security context.
     * Note: User JWT is not yet implemented.
     */
    private Long resolveCurrentUserId() {
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户端 AI 功能暂未开放，请等待用户登录功能上线");
    }
}
