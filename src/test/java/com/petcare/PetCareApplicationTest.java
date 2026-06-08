package com.petcare;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Verifies that the Spring ApplicationContext can start successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class PetCareApplicationTest {

    @Test
    void contextLoads() {
        // If the application context fails to start, this test will fail.
        assertDoesNotThrow(() -> {
            // No-op: @SpringBootTest itself validates context startup
        });
    }
}
