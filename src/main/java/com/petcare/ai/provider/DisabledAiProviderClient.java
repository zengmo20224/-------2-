package com.petcare.ai.provider;

/**
 * Default AI Provider implementation when the real provider is not enabled.
 * Always throws {@link AiProviderUnavailableException} to signal that
 * AI features are currently unavailable.
 *
 * This is the active bean when {@code petcare.ai.provider-enabled} is false.
 * It never fakes a successful AI response.
 */
public class DisabledAiProviderClient implements AiProviderClient {

    @Override
    public AiProviderResponse complete(AiProviderRequest request) {
        throw new AiProviderUnavailableException("AI 服务暂未启用");
    }
}
