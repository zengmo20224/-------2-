package com.petcare.booking.domain;

import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates distance between two geographic coordinates using the Haversine formula.
 * Provides home service distance validation.
 */
public final class BookingDistanceCalculator {

    private static final BigDecimal EARTH_RADIUS_KM = new BigDecimal("6371.00");

    private BookingDistanceCalculator() {
        // utility class
    }

    /**
     * Calculates the great-circle distance between two points using the Haversine formula.
     * Returns distance in kilometers, rounded to 2 decimal places.
     *
     * @param lat1 latitude of point 1 in degrees
     * @param lon1 longitude of point 1 in degrees
     * @param lat2 latitude of point 2 in degrees
     * @param lon2 longitude of point 2 in degrees
     * @return distance in kilometers
     */
    public static BigDecimal calculateDistance(BigDecimal lat1, BigDecimal lon1,
                                               BigDecimal lat2, BigDecimal lon2) {
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double deltaLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double deltaLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        BigDecimal distance = EARTH_RADIUS_KM.multiply(BigDecimal.valueOf(c));
        return distance.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Validates that the home service distance does not exceed the configured radius.
     *
     * @param distanceKm   calculated distance in km
     * @param maxRadiusKm  maximum allowed distance from store_config.home_service_radius_km
     * @throws BusinessException if distance exceeds the radius
     */
    public static void validateHomeServiceDistance(BigDecimal distanceKm, BigDecimal maxRadiusKm) {
        if (distanceKm.compareTo(maxRadiusKm) > 0) {
            throw new BusinessException(
                    ErrorCode.BOOKING_HOME_DISTANCE_EXCEEDED,
                    String.format("当前地址距离门店约 %.2f 公里，超出本店 %.2f 公里上门服务范围，请更换地址或选择到店服务。",
                            distanceKm, maxRadiusKm));
        }
    }
}
