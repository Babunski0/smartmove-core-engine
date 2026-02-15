package com.smartmove.dto.response;

import com.smartmove.domain.enums.City;
import com.smartmove.domain.enums.VehicleState;
import com.smartmove.domain.enums.VehicleType;
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
 *  Return list of Vehicles (All, By city, by state = AVAILABLE)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleListResponse {
    private String vehicleId;
    private VehicleType type;
    private City city;
    private VehicleState state;
    private Double batteryPercentage;
    private Boolean locked;
    private Double hourlyRate;
}
