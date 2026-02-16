package com.smartmove.dto.response;

import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleListResponse {
    private String vehicleId;
    private String type;
    private String city;
    private String state;
    private Double batteryPercentage;
    private boolean locked;
    private Double hourlyRate;
}
