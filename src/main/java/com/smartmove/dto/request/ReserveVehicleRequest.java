package com.smartmove.dto.request;

import com.smartmove.domain.enums.City;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveVehicleRequest {

    @NotBlank(message = "Vehicle ID cannot be blank")
    private String vehicleId;

    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @NotNull(message = "City cannot be null")
    private City city;
}
