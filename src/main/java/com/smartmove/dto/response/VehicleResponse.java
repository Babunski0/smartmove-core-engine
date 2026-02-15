package com.smartmove.dto.response;

import com.smartmove.domain.enums.City;
import com.smartmove.domain.enums.VehicleState;
import com.smartmove.domain.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 14.02.2026
 */


/**
 *  Return Vehicle Data -JSON response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {

    // Basic info
    private String vehicleId;
    private VehicleType type;      // BICYCLE, ELECTRIC_SCOOTER, MOPED
    private City city;

    // Current state
    private VehicleState state;
    private Boolean locked;

    // Location
    private Double latitude;
    private Double longitude;
    private Long locationTimestamp;

    // Status
    private Double batteryPercentage;
    private Double temperatureCelsius;
    private String activeRentalId;  // If currently rented

    // Business logic
    private Double hourlyRate;
    private Integer maintenanceIntervalHours;
    private List<String> requiredMaintenanceChecks;
}
