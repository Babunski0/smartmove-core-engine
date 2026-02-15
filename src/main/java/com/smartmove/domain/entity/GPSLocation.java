package com.smartmove.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 13.02.2026
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GPSLocation {
    private double latitude;
    private double longitude;
    private long timestamp;

    public GPSLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
    }

    public double distanceTo(GPSLocation location) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(location.latitude - this.latitude);
        double lonDistance = Math.toRadians(location.longitude - this.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude))
                * Math.cos(Math.toRadians(location.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
