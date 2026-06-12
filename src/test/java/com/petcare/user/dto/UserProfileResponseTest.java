package com.petcare.user.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserProfileResponse.maskPhone pure function.
 */
class UserProfileResponseTest {

    @Test
    @DisplayName("standard 11-digit phone is masked correctly")
    void standardPhoneMasked() {
        assertThat(UserProfileResponse.maskPhone("13800138001")).isEqualTo("138****8001");
    }

    @Test
    @DisplayName("null phone returns null")
    void nullPhoneReturnsNull() {
        assertThat(UserProfileResponse.maskPhone(null)).isNull();
    }

    @Test
    @DisplayName("blank phone returns null")
    void blankPhoneReturnsNull() {
        assertThat(UserProfileResponse.maskPhone("   ")).isNull();
    }

    @Test
    @DisplayName("empty phone returns null")
    void emptyPhoneReturnsNull() {
        assertThat(UserProfileResponse.maskPhone("")).isNull();
    }

    @Test
    @DisplayName("phone shorter than 7 digits returns null — does not expose partial number")
    void shortPhoneReturnsNull() {
        assertThat(UserProfileResponse.maskPhone("13800")).isNull();
    }

    @Test
    @DisplayName("exactly 7-digit phone is masked")
    void exactly7DigitPhoneMasked() {
        assertThat(UserProfileResponse.maskPhone("1380013")).isEqualTo("138****0013");
    }
}
