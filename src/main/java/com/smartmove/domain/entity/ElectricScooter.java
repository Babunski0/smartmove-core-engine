package com.smartmove.domain.entity;

import com.smartmove.domain.enums.City;
import com.smartmove.domain.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ElectricScooter extends Vehicle {

    private int motorPowerWatts;
    private double maxSpeedKmh;

    public ElectricScooter(String vehicleId, City city, GPSLocation location) {
        super(vehicleId, VehicleType.ELECTRIC_SCOOTER, city, location);
        this.motorPowerWatts = 350; // default
        this.maxSpeedKmh = 25.0;    // default EU limit
    }

    @Override
    public double getHourlyRate() {
        return 3.50;
    }

    @Override
    public int getMaintenanceIntervalHours() {
        return 100;
    }

    @Override
    public List<String> getRequiredMaintenanceChecks() {
        return List.of(
                "Motor Check",
                "Battery Health",
                "Brake Pads",
                "Tire Pressure"
        );
    }
}
