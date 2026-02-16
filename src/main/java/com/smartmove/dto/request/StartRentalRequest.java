package com.smartmove.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartRentalRequest {

    @NotBlank(message = "Vehicle ID cannot be blank")
    private String vehicleId;
}
