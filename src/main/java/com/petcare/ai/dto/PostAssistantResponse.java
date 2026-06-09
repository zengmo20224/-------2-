package com.petcare.ai.dto;

/**
 * Response DTO for AI post assistant.
 * The generated content is a suggested draft, not a published post.
 */
public record PostAssistantResponse(
        String suggestedText,
        boolean isDraft
) {
    public PostAssistantResponse(String suggestedText) {
        this(suggestedText, true);
    }
}
