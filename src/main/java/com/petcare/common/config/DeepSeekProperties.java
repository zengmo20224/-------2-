package com.petcare.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DeepSeek AI Provider configuration properties.
 * API key is optional in phase 2; real AI calls start in phase 8.
 */
@ConfigurationProperties(prefix = "petcare.ai.deepseek")
public record DeepSeekProperties(String baseUrl, String apiKey) {
}
