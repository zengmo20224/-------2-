package com.petcare.ai.provider;

/**
 * Provider-agnostic response model.
 * Does not expose raw provider errors, headers, or sensitive data.
 */
public record AiProviderResponse(
        String assistantText,
        String modelName,
        AiProviderUsage usage,
        String providerRequestId
) {

    public AiProviderResponse {
        if (assistantText == null) {
            throw new IllegalArgumentException("assistantText must not be null");
        }
    }
}
