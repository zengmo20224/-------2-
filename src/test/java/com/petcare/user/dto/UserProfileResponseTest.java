package com.petcare.user.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserProfileResponse.maskPhone pure function.
 * Only valid Chinese mainland phone numbers (^1\\d{10}$) are masked.
 * All other values return null to prevent data leakage.
 */
class UserProfileResponseTest {

    // ======================== Valid phones ========================

    @Test
    @DisplayName("standard 11-digit phone starting with 1 is masked correctly")
    void standardPhoneMasked() {
        assertThat(UserProfileResponse.maskPhone("13800138001")).isEqualTo("138****8001");
    }

    @Test
    @DisplayName("different valid prefix also masked correctly")
    void differentValidPrefix() {
        assertThat(UserProfileResponse.maskPhone("15912345678")).isEqualTo("159****5678");
    }

    // ======================== Invalid lengths ========================

    @Test
    @DisplayName("7-digit number returns null — does not expose partial number")
    void sevenDigitPhoneReturnsNull() {
        assertThat(UserProfileResponse.maskPhone("1380013")).isNull();
    }

    @Test
    @DisplayName("8-digit number returns null")
    void eightDigitPhoneReturnsNull() {
        assertThat(UserProfileResponse.maskPhone("138001380")).isNull();
    }

    @Test
    @DisplayName("10-digit number returns null")
    void tenDigitPhoneReturnsNull() {
        assertThat(UserProfileResponse.maskPhone("1380013800")).isNull();
    }

    @Test
    @DisplayName("12-digit number returns null")
    void twelveDigitPhoneReturnsNull() {
        assertThat(UserProfileResponse.maskPhone("138001380011")).isNull();
    }

    @Test
    @DisplayName("5-digit short number returns null")
    void shortPhoneReturnsNull() {
        assertThat(UserProfileResponse.maskPhone("13800")).isNull();
    }

    // ======================== Invalid formats ========================

    @Test
    @DisplayName("11-digit not starting with 1 returns null")
    void elevenDigitsNotStartingWith1() {
        assertThat(UserProfileResponse.maskPhone("23800138001")).isNull();
    }

    @Test
    @DisplayName("phone containing letters returns null")
    void phoneWithLettersReturnsNull() {
        assertThat(UserProfileResponse.maskPhone("13800abc8001")).isNull();
    }

    @Test
    @DisplayName("phone with leading whitespace returns null")
    void phoneWithLeadingWhitespaceReturnsNull() {
        assertThat(UserProfileResponse.maskPhone(" 13800138001")).isNull();
    }

    @Test
    @DisplayName("phone with trailing whitespace returns null")
    void phoneWithTrailingWhitespaceReturnsNull() {
        assertThat(UserProfileResponse.maskPhone("13800138001 ")).isNull();
    }

    @Test
    @DisplayName("phone with spaces in middle returns null")
    void phoneWithMiddleSpacesReturnsNull() {
        assertThat(UserProfileResponse.maskPhone("138 0013 8001")).isNull();
    }

    // ======================== Null / blank ========================

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
}
