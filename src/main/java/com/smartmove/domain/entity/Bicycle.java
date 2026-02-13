package com.smartmove.domain.entity;

import com.smartmove.domain.enums.City;
import com.smartmove.domain.enums.VehicleType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 13.02.2026
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class Bicycle extends Vehicle {

    private double wheelSize;
    private boolean hasGear;

    public Bicycle(String vehicleId, City city, GPSLocation location) {
        super(vehicleId, VehicleType.BICYCLE, city, location);
        this.wheelSize = 26.0;
        this.hasGear = true;
    }

    @Override
    public double getHourlyRate() {
        return 2.50;
    }

    @Override
    public int getMaintenanceIntervalHours() {
        return 168;
    }

    @Override
    public List<String> getRequiredMaintenanceChecks() {
        return List.of("Tire Pressure", "Chain Lubrication", "Brake Pads", "Frame Inspection");
    }
}
