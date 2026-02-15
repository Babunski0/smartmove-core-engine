package com.smartmove.domain.entity;

import lombok.Data;

import java.util.List;

@Data
public class Telemetry {

    private String vehicleId;
    private long timestamp;

    private double latitude;
    private double longitude;

    private double batteryPercentage;
    private double temperatureCelsius;
    private double speedKmh;

    private boolean helmetDetected;
    private List<String> activeSensors;

    public Telemetry(String vehicleId,
                     long timestamp,
                     double latitude,
                     double longitude,
                     double batteryPercentage,
                     double temperatureCelsius,
                     double speedKmh,
                     boolean helmetDetected,
                     List<String> activeSensors) {

        this.vehicleId = vehicleId;
        this.timestamp = timestamp;

        validateLatitude(latitude);
        validateLongitude(longitude);
        validateBattery(batteryPercentage);

        this.latitude = latitude;
        this.longitude = longitude;
        this.batteryPercentage = batteryPercentage;
        this.temperatureCelsius = temperatureCelsius;
        this.speedKmh = speedKmh;
        this.helmetDetected = helmetDetected;
        this.activeSensors = activeSensors;
    }

    public GPSLocation getLocation() {
        return new GPSLocation(latitude, longitude);
    }

    private void validateLatitude(double latitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
    }

    private void validateLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }

    private void validateBattery(double battery) {
        if (battery < 0 || battery > 100) {
            throw new IllegalArgumentException("Battery percentage must be between 0 and 100");
        }
    }
}
