package com.petcare.booking.domain;

import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for BookingDistanceCalculator.
 * Pure logic tests — no Spring context needed.
 */
class BookingDistanceCalculatorTest {

    @Nested
    @DisplayName("Haversine distance calculation")
    class CalculateDistance {

        @Test
        @DisplayName("Same coordinates return approximately 0 km")
        void sameCoordinatesReturnZero() {
            BigDecimal lat = new BigDecimal("39.904200");
            BigDecimal lon = new BigDecimal("116.407400");
            BigDecimal distance = BookingDistanceCalculator.calculateDistance(lat, lon, lat, lon);
            assertThat(distance).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Beijing to Shanghai is approximately 1068 km")
        void beijingToShanghai() {
            // Beijing: 39.9042 N, 116.4074 E
            // Shanghai: 31.2304 N, 121.4737 E
            BigDecimal distance = BookingDistanceCalculator.calculateDistance(
                    new BigDecimal("39.904200"), new BigDecimal("116.407400"),
                    new BigDecimal("31.230400"), new BigDecimal("121.473700"));
            // Known distance: ~1068 km, allow ±10 km tolerance
            assertThat(distance.doubleValue()).isBetween(1050.0, 1090.0);
        }

        @Test
        @DisplayName("Short distance within same city")
        void shortDistanceWithinCity() {
            // Two points about 1.1 km apart
            BigDecimal distance = BookingDistanceCalculator.calculateDistance(
                    new BigDecimal("39.904200"), new BigDecimal("116.407400"),
                    new BigDecimal("39.914200"), new BigDecimal("116.407400"));
            // ~1.11 km (0.01 degree latitude difference at this latitude)
            assertThat(distance.doubleValue()).isBetween(0.5, 2.0);
        }

        @Test
        @DisplayName("Result is rounded to 2 decimal places")
        void resultRoundedToTwoDecimals() {
            BigDecimal distance = BookingDistanceCalculator.calculateDistance(
                    new BigDecimal("39.904200"), new BigDecimal("116.407400"),
                    new BigDecimal("31.230400"), new BigDecimal("121.473700"));
            assertThat(distance.scale()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Home service distance validation")
    class ValidateHomeServiceDistance {

        @Test
        @DisplayName("Distance within radius passes without exception")
        void withinRadiusPasses() {
            BigDecimal distance = new BigDecimal("3.50");
            BigDecimal maxRadius = new BigDecimal("5.00");
            assertThatCode(() -> BookingDistanceCalculator.validateHomeServiceDistance(distance, maxRadius))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Distance exactly at radius passes")
        void exactlyAtRadiusPasses() {
            BigDecimal distance = new BigDecimal("5.00");
            BigDecimal maxRadius = new BigDecimal("5.00");
            assertThatCode(() -> BookingDistanceCalculator.validateHomeServiceDistance(distance, maxRadius))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Distance exceeding radius throws BOOKING_HOME_DISTANCE_EXCEEDED")
        void exceedsRadiusThrows() {
            BigDecimal distance = new BigDecimal("9.20");
            BigDecimal maxRadius = new BigDecimal("5.00");
            assertThatThrownBy(() -> BookingDistanceCalculator.validateHomeServiceDistance(distance, maxRadius))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.BOOKING_HOME_DISTANCE_EXCEEDED);
        }

        @Test
        @DisplayName("Error message contains distance and radius values")
        void errorMessageContainsValues() {
            BigDecimal distance = new BigDecimal("9.20");
            BigDecimal maxRadius = new BigDecimal("5.00");
            assertThatThrownBy(() -> BookingDistanceCalculator.validateHomeServiceDistance(distance, maxRadius))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("9.20")
                    .hasMessageContaining("5.00")
                    .hasMessageContaining("超出本店")
                    .hasMessageContaining("上门服务范围");
        }
    }
}
