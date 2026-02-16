package com.smartmove.dto.response;

import lombok.*;

import java.time.Instant; // ili šta već koristiš

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private String vehicleId;
    private String type;
    private String city;
    private String state;
    private boolean locked;

    private double latitude;
    private double longitude;
    private Instant locationTimestamp;

    private Double batteryPercentage;
    private Double temperatureCelsius;
    private String activeRentalId;

    private Double hourlyRate;
    private Integer maintenanceIntervalHours;
    private Integer requiredMaintenanceChecks;
}
