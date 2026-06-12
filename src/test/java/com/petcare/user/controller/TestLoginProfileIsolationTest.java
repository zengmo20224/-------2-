package com.petcare.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RED-3: Tests that test-login endpoint does not exist outside test profile.
 * Uses "profile-isolation" profile which does not activate TestLoginController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("profile-isolation")
class TestLoginProfileIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("test-login returns 404 in non-test profile")
    void testLoginReturns404InNonTestProfile() throws Exception {
        mockMvc.perform(post("/api/v1/auth/test-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138001\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TestLoginController bean does not exist in non-test profile")
    void testLoginControllerBeanDoesNotExist() {
        boolean hasController = applicationContext.containsBeanDefinition("testLoginController");
        assertThat(hasController).isFalse();
    }
}
