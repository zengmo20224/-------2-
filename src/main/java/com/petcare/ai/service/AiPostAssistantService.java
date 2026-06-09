package com.petcare.ai.service;

import com.petcare.ai.dto.PostAssistantRequest;
import com.petcare.ai.dto.PostAssistantResponse;

/**
 * Service for AI post content generation.
 * Only uses facts explicitly provided by the user.
 * Generated content is a suggested draft, not a published post.
 */
public interface AiPostAssistantService {

    /**
     * Generates a suggested post draft based on user-provided facts.
     *
     * @param currentUserId the user ID from security context
     * @param request       post assistant request with user-provided facts
     * @return suggested draft text, always marked as draft
     */
    PostAssistantResponse generateDraft(Long currentUserId, PostAssistantRequest request);
}
