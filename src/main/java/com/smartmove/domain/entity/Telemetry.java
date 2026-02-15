package com.smartmove.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Telemetry {

    private String vehicleId;
    private long timestamp;

    private double latitude;
    private double longitude;

    private double batteryPercentage;
    private double temperatureCelsius;
    private double speedKmh;

    private boolean helmetDetected;
    @Builder.Default
    private List<String> activeSensors = new ArrayList<>();

    public Telemetry(String vehicleId,
                     long timestamp,
                     double latitude,
                     double longitude,
                     double batteryPercentage,
                     double temperatureCelsius) {

        this.vehicleId = vehicleId;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.batteryPercentage = batteryPercentage;
        this.temperatureCelsius = temperatureCelsius;
        this.activeSensors = new ArrayList<>();
    }
}
