package com.petcare.ai.provider;

/**
 * Provider-agnostic AI client interface.
 * Business code depends on this port, never on concrete HTTP clients.
 */
public interface AiProviderClient {

    /**
     * Sends a completion request and returns the assistant response.
     *
     * @param request provider-agnostic request
     * @return provider-agnostic response
     * @throws AiProviderUnavailableException if the provider is disabled or unreachable
     * @throws AiProviderException if the provider returns an upstream error
     */
    AiProviderResponse complete(AiProviderRequest request);
}
