package com.smartmove.dto.request;

import com.smartmove.domain.enums.VehicleState;
import jakarta.validation.constraints.*;
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
 *  Update Vehicle Location & Status (Admin & System)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVehicleRequest {

    @NotBlank(message = "Vehicle ID cannot be blank")
    private String vehicleId;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;

    @Min(0)
    @Max(100)
    private Double batteryPercentage;

    @DecimalMin("-50.0")
    @DecimalMax("150.0")
    private Double temperatureCelsius;

    private VehicleState state;  // AVAILABLE, RESERVED, IN_USE, etc.

    private Boolean locked;
}
