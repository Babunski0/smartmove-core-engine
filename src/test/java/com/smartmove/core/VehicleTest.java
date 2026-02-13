package com.smartmove.core;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class VehicleTest {

    @Test
    public void newVehicle_isAvailableByDefault() {
        Vehicle v = new Vehicle("V1", VehicleType.E_SCOOTER);
        assertEquals(VehicleState.AVAILABLE, v.getState());
    }

    @Test
    public void canChangeState() {
        Vehicle v = new Vehicle("V1", VehicleType.BICYCLE);
        v.setState(VehicleState.RESERVED);
        assertEquals(VehicleState.RESERVED, v.getState());
    }
}
