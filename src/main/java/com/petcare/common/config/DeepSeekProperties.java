package com.petcare.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DeepSeek AI Provider configuration properties.
 * API key and other sensitive fields must come from environment variables.
 * No default values for undecided fields (D-008).
 */
@ConfigurationProperties(prefix = "petcare.ai.deepseek")
public record DeepSeekProperties(
        String baseUrl,
        String apiKey,
        String model,
        Integer connectTimeout,
        Integer readTimeout,
        Integer maxTokens,
        Integer maxRetries
) {}
