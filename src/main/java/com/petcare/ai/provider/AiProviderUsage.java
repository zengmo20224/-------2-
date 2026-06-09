package com.petcare.ai.provider;

/**
 * Token usage returned by the Provider.
 */
public record AiProviderUsage(int promptTokens, int completionTokens, int totalTokens) {

    public static AiProviderUsage empty() {
        return new AiProviderUsage(0, 0, 0);
    }
}
