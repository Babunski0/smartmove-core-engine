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
public class Moped extends Vehicle {

    private int engineCcm;
    private boolean hasHelmetSensor;

    public Moped(String vehicleId, City city, GPSLocation location) {
        super(vehicleId, VehicleType.MOPED, city, location);
        this.engineCcm = 125;
        this.hasHelmetSensor = true;
    }

    @Override
    public double getHourlyRate() {
        return 5.00;
    }

    @Override
    public int getMaintenanceIntervalHours() {
        return 50;
    }

    @Override
    public List<String> getRequiredMaintenanceChecks() {
        return List.of(
                "Oil level",
                "Spark Plug",
                "Brake Fluid",
                "Helmet Sensor",
                "Tire Pressure"
        );
    }
}
