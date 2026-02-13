package com.smartmove.controller;

import com.smartmove.core.Vehicle;
import com.smartmove.core.VehicleState;
import com.smartmove.core.VehicleType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SmartMoveCentralControllerTest {

    @Test
    public void reserve_movesAvailableToReserved() {
        Vehicle v = new Vehicle("V1", VehicleType.E_SCOOTER);
        SmartMoveCentralController c = new SmartMoveCentralController();

        c.reserve(v);

        assertEquals(VehicleState.RESERVED, v.getState());
    }

    @Test(expected = IllegalStateException.class)
    public void reserve_failsIfNotAvailable() {
        Vehicle v = new Vehicle("V1", VehicleType.E_SCOOTER);
        v.setState(VehicleState.MAINTENANCE);

        new SmartMoveCentralController().reserve(v);
    }

    @Test
    public void cancelReservation_movesReservedToAvailable() {
        Vehicle v = new Vehicle("V1", VehicleType.BICYCLE);
        v.setState(VehicleState.RESERVED);

        SmartMoveCentralController c = new SmartMoveCentralController();
        c.cancelReservation(v);

        assertEquals(VehicleState.AVAILABLE, v.getState());
    }

    @Test(expected = IllegalStateException.class)
    public void cancelReservation_failsIfNotReserved() {
        Vehicle v = new Vehicle("V1", VehicleType.BICYCLE);

        new SmartMoveCentralController().cancelReservation(v);
    }
}
