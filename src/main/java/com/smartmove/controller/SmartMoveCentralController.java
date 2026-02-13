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
}
