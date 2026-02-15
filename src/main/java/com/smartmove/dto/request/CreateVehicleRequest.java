package com.smartmove.dto.request;

import com.smartmove.domain.enums.City;
import com.smartmove.domain.enums.VehicleType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 14.02.2026
 */

/**
 *  Only for Admin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVehicleRequest {

    @NotBlank(message = "Vehicle ID cannot be blank")
    private String vehicleId;

    @NotNull(message = "Vehicle type cannot be null")
    private VehicleType type;  // BICYCLE, ELECTRIC_SCOOTER, MOPED

    @NotNull(message = "City cannot be null")
    private City city;

    @NotNull(message = "Initial latitude cannot be null")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @NotNull(message = "Initial longitude cannot be null")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;

    // Type-specific fields (optional)
    private Double wheelSize;              // For Bicycle
    private Boolean hasGear;               // For Bicycle
    private Double motorPowerWatts;        // For ElectricScooter
    private Double maxSpeedKmh;            // For ElectricScooter
    private Double engineCcm;              // For Moped
    private Boolean hasHelmetSensor;       // For Moped
}
