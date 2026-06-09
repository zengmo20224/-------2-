package com.petcare.ai.provider;

/**
 * Base exception for AI Provider failures.
 * Does not carry API keys, raw responses, or stack details.
 */
public class AiProviderException extends RuntimeException {

    private final String internalCode;

    public AiProviderException(String internalCode, String message) {
        super(message);
        this.internalCode = internalCode;
    }

    public String getInternalCode() {
        return internalCode;
    }
}
