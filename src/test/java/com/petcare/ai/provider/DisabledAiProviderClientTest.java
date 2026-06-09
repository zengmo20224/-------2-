package com.petcare.ai.provider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the disabled AI provider client.
 * Verifies that it never fakes a successful AI response.
 */
class DisabledAiProviderClientTest {

    private final DisabledAiProviderClient client = new DisabledAiProviderClient();

    @Test
    @DisplayName("complete() throws AiProviderUnavailableException")
    void complete_throwsUnavailable() {
        AiProviderRequest request = new AiProviderRequest(
                AiApiType.CHAT,
                List.of(new AiProviderMessage("user", "hello")),
                "test-correlation"
        );

        assertThrows(AiProviderUnavailableException.class, () -> client.complete(request));
    }

    @Test
    @DisplayName("Exception message is user-friendly")
    void exceptionMessage_isUserFriendly() {
        AiProviderRequest request = new AiProviderRequest(
                AiApiType.CUSTOMER_SERVICE,
                List.of(new AiProviderMessage("user", "test")),
                null
        );

        AiProviderUnavailableException ex = assertThrows(
                AiProviderUnavailableException.class,
                () -> client.complete(request)
        );

        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().contains("API"));
        assertFalse(ex.getMessage().contains("key"));
        assertFalse(ex.getMessage().contains("http"));
    }
}
