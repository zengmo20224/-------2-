package com.petcare.ai.provider;

import java.util.List;

/**
 * Provider-agnostic request model.
 * Business code never specifies model name, API key, base URL, or tools.
 */
public record AiProviderRequest(
        AiApiType apiType,
        List<AiProviderMessage> messages,
        String correlationId
) {

    public AiProviderRequest {
        if (apiType == null) {
            throw new IllegalArgumentException("apiType must not be null");
        }
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages must not be null or empty");
        }
    }
}
