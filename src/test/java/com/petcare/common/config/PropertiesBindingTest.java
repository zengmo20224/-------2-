package com.petcare.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests that configuration properties are correctly bound
 * from application.yml / application-test.yml.
 */
@SpringBootTest
@ActiveProfiles("test")
class PropertiesBindingTest {

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private DeepSeekProperties deepSeekProperties;

    @Test
    void securityProperties_shouldBindFromYaml() {
        assertNotNull(securityProperties);
        // JWT_SECRET defaults to empty string in test profile
        assertEquals("", securityProperties.jwtSecret());
    }

    @Test
    void deepSeekProperties_shouldBindFromYaml() {
        assertNotNull(deepSeekProperties);
        assertEquals("https://api.deepseek.com", deepSeekProperties.baseUrl());
        assertEquals("", deepSeekProperties.apiKey());
    }
}
