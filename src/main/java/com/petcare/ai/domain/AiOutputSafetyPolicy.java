package com.petcare.ai.domain;

/**
 * General output safety policy for all AI responses.
 * Checks for prompt injection leakage, secret exposure, and other safety issues.
 */
public final class AiOutputSafetyPolicy {

    private static final String[] LEAKAGE_INDICATORS = {
            "system prompt",
            "系统指令",
            "你是一个",
            "you are a",
            "ignore previous",
            "忽略之前的",
            "apiKey",
            "api_key",
            "authorization",
            "bearer"
    };

    private AiOutputSafetyPolicy() {
        // prevent instantiation
    }

    /**
     * Checks if the AI output contains prompt leakage or secret exposure.
     *
     * @param aiOutput the text returned by the AI Provider
     * @return true if the output should be rejected
     */
    public static boolean isUnsafe(String aiOutput) {
        if (aiOutput == null || aiOutput.isBlank()) {
            return false;
        }
        String lower = aiOutput.toLowerCase();
        for (String indicator : LEAKAGE_INDICATORS) {
            if (lower.contains(indicator.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
