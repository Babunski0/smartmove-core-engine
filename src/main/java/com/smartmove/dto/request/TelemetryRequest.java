package com.smartmove.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryRequest {

    @NotBlank(message = "Vehicle ID cannot be blank")
    private String vehicleId;

    @NotNull(message = "Latitude cannot be null")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;

    @NotNull(message = "Longitude cannot be null")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;

    @NotNull(message = "Battery percentage cannot be null")
    @Min(0)
    @Max(100)
    private Double batteryPercentage;

    @NotNull(message = "Temperature cannot be null")
    @DecimalMin("-50.0")
    @DecimalMax("150.0")
    private Double temperatureCelsius;

    @NotNull(message = "Speed cannot be null")
    @Min(0)
    private Double speedKmh;

    private Boolean helmetDetected;
}
