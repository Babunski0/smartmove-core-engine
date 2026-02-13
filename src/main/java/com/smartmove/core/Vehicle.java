package com.smartmove.core;

import java.util.Objects;

public final class Vehicle {
    private final String id;
    private final VehicleType type;
    private VehicleState state;

    public Vehicle(String id, VehicleType type) {
        this.id = Objects.requireNonNull(id);
        this.type = Objects.requireNonNull(type);
        this.state = VehicleState.AVAILABLE;
    }

    public String getId() { return id; }
    public VehicleType getType() { return type; }
    public VehicleState getState() { return state; }

    public void setState(VehicleState state) {
        this.state = Objects.requireNonNull(state);
    }
}
