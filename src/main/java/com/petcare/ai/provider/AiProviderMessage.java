package com.petcare.ai.provider;

/**
 * A single message in the Provider request.
 * Mirrors the OpenAI Chat Completions message structure.
 */
public record AiProviderMessage(String role, String content) {
}
