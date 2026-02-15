package com.smartmove.domain.entity;

import com.smartmove.domain.enums.City;
import com.smartmove.domain.enums.VehicleState;
import com.smartmove.domain.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 13.02.2026
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Vehicle {

    protected String vehicleId;
    protected VehicleType type;
    protected City city;
    protected VehicleState currentState;
    protected GPSLocation currentLocation;
    protected double batteryPercentage;
    protected double temperatureCelsius;
    protected String activeRentalId;
    protected long lastTelemetryUpdate;
    protected boolean locked;

    public Vehicle(String vehicleId, VehicleType type, City city,
                   GPSLocation location) {
        this.vehicleId = vehicleId;
        this.type = type;
        this.city = city;
        this.currentLocation = location;
        this.currentState = VehicleState.AVAILABLE;
        this.batteryPercentage = 100.0;
        this.temperatureCelsius = 25.0;
        this.locked = true;
        this.lastTelemetryUpdate = System.currentTimeMillis();
    }

    public abstract double getHourlyRate();
    public abstract int getMaintenanceIntervalHours();
    public abstract List<String> getRequiredMaintenanceChecks();

    protected void setState(VehicleState newState) {
        this.currentState = newState;
    }

    protected void setLocation(GPSLocation location) {
        this.currentLocation = location;
    }

    protected void setBatteryPercentage(double percentage) {
        this.batteryPercentage = Math.max(0, Math.min(100, percentage));
    }

    protected void setTemperature(double celsius) {
        this.temperatureCelsius = celsius;
    }

    protected void setActiveRentalId(String rentalId) {
        this.activeRentalId = rentalId;
    }

    protected void setLocked(boolean locked) {
        this.locked = locked;
    }

    protected void updateLastTelemetry() {
        this.lastTelemetryUpdate = System.currentTimeMillis();
    }
}
