package com.smartmove.controller;

import com.smartmove.core.Vehicle;
import com.smartmove.core.VehicleState;

import java.util.Objects;

public class SmartMoveCentralController {

    public void reserve(Vehicle vehicle) {
        Objects.requireNonNull(vehicle);

        if (vehicle.getState() != VehicleState.AVAILABLE) {
            throw new IllegalStateException("Vehicle must be AVAILABLE to reserve");
        }
        vehicle.setState(VehicleState.RESERVED);
    }

    public void cancelReservation(Vehicle vehicle) {
        Objects.requireNonNull(vehicle);

        if (vehicle.getState() != VehicleState.RESERVED) {
            throw new IllegalStateException("Vehicle must be RESERVED to cancel reservation");
        }
        vehicle.setState(VehicleState.AVAILABLE);
    }
    
    public void startRental(Vehicle vehicle) {
        Objects.requireNonNull(vehicle);

        if (vehicle.getState() != VehicleState.RESERVED) {
            throw new IllegalStateException("Vehicle must be RESERVED to start rental");
        }
        vehicle.setState(VehicleState.IN_USE);
    }

    public void endRental(Vehicle vehicle) {
        Objects.requireNonNull(vehicle);

        if (vehicle.getState() != VehicleState.IN_USE) {
            throw new IllegalStateException("Vehicle must be IN_USE to end rental");
        }
        vehicle.setState(VehicleState.AVAILABLE);
    }
}
