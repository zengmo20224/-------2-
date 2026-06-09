package com.petcare.ai.provider;

/**
 * Thrown when the AI Provider is not enabled or not reachable.
 */
public class AiProviderUnavailableException extends AiProviderException {

    public AiProviderUnavailableException(String message) {
        super("PROVIDER_UNAVAILABLE", message);
    }
}
